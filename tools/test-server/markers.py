"""Writes BlueMap markers for a test-server world (docs/TEST-SERVER-PLAN.md) into its map config.

Usage: python markers.py <server-dir> <radius> <gametest-log>
  e.g. python tools/test-server/markers.py ../finite-test-server 3000 build/run/gameTest/logs/latest.log

Markers, one toggleable marker set each: the radius and the generated edge (radius + 512), spawn, the
47 biome anchors (from AnchorPlanGameTest's "[SRV] seed 12345 radius N" log line), and every structure
start saved in the world's region files, one set per structure type. Sets with more than 100 markers
(mineshafts, buried treasure, ...) start hidden. Replaces the marker-sets block at the end of
config/bluemap/maps/rN.conf; run `bluemap reload` on the server afterwards. No re-render is needed.
"""
import glob, gzip, io, json, math, os, re, struct, sys, zlib
from collections import defaultdict


def nbt(f, t):
    """One NBT payload of tag type t. Arrays (block states, heightmaps) are skipped and read as None."""
    if t == 1: return struct.unpack('>b', f.read(1))[0]
    if t == 2: return struct.unpack('>h', f.read(2))[0]
    if t == 3: return struct.unpack('>i', f.read(4))[0]
    if t == 4: return struct.unpack('>q', f.read(8))[0]
    if t == 5: return struct.unpack('>f', f.read(4))[0]
    if t == 6: return struct.unpack('>d', f.read(8))[0]
    if t in (7, 11, 12):
        n = nbt(f, 3)
        if t == 11: return [nbt(f, 3) for _ in range(n)]  # int arrays hold bounding boxes, keep them
        f.seek(n * (1 if t == 7 else 8), 1)
        return None
    if t == 8: return f.read(struct.unpack('>H', f.read(2))[0]).decode('utf-8', 'replace')
    if t == 9:
        et, n = nbt(f, 1), nbt(f, 3)
        return [nbt(f, et) for _ in range(n)]
    if t == 10:
        d = {}
        while (ct := nbt(f, 1)) != 0:
            k = nbt(f, 8)  # the name comes first; d[nbt()] = nbt() would read the value first
            d[k] = nbt(f, ct)
        return d
    raise ValueError(f'tag type {t}')


def root(data):
    f = io.BytesIO(data)
    t = nbt(f, 1); nbt(f, 8)
    return nbt(f, t)


def structure_starts(world):
    """(id, x, y, z) for every structure start: the centre of its pieces' bounding boxes, at their top."""
    for path in glob.glob(os.path.join(world, 'dimensions', 'minecraft', 'overworld', 'region', 'r.*.mca')):
        with open(path, 'rb') as f:
            header = f.read(4096)
            for i in range(1024):
                loc = struct.unpack('>I', header[i * 4:i * 4 + 4])[0]
                if not loc: continue
                f.seek((loc >> 8) * 4096)
                length, compression = struct.unpack('>IB', f.read(5))
                data = zlib.decompress(f.read(length - 1)) if compression == 2 else gzip.decompress(f.read(length - 1))
                if b'ChunkX' not in data: continue  # only chunks holding a start have one
                chunk = root(data)
                for sid, start in chunk.get('structures', {}).get('starts', {}).items():
                    boxes = [c['BB'] for c in start.get('Children', []) if c.get('BB')]
                    if not boxes: continue
                    assert (start['ChunkX'], start['ChunkZ']) == (chunk['xPos'], chunk['zPos']), (path, sid, start['ChunkX'])
                    x0, z0 = min(b[0] for b in boxes), min(b[2] for b in boxes)
                    x1, z1 = max(b[3] for b in boxes), max(b[5] for b in boxes)
                    yield sid, (x0 + x1) // 2, max(b[4] for b in boxes), (z0 + z1) // 2


def anchors(log, radius):
    """[(biome, x, z, r)] from the AnchorPlanGameTest line for seed 12345 at this radius."""
    line = next(l for l in open(log, encoding='utf-8', errors='replace') if f'[SRV] seed 12345 radius {radius}:' in l)
    return [(b, int(x), int(z), int(r)) for b, x, z, r in
            re.findall(r'Anchor\[biome=([\w:]+), x=(-?\d+), z=(-?\d+), radius=(\d+)', line)]


def spawn(world):
    with gzip.open(os.path.join(world, 'level.dat')) as f:
        return root(f.read())['Data']['spawn']['pos']


def ring(r, n=256, y=None):
    pts = [(round(r * math.cos(2 * math.pi * i / n)), round(r * math.sin(2 * math.pi * i / n))) for i in range(n)]
    return [{'x': x, 'z': z} if y is None else {'x': x, 'y': y, 'z': z} for x, z in pts + pts[:1]]


def label(sid):
    return sid.split(':')[-1].replace('_', ' ').capitalize()


def circle(r, name, rgb):
    return {'type': 'line', 'label': name, 'position': {'x': 0, 'y': 64, 'z': 0}, 'line': ring(r, y=64),
            'line-width': 3, 'line-color': {'r': rgb[0], 'g': rgb[1], 'b': rgb[2], 'a': 1.0}, 'max-distance': 100000}


def marker_sets(radius, world, log):
    sx, sy, sz = spawn(world)
    sets = {'finite-world': {'label': 'Finite World', 'toggleable': True, 'sorting': 0, 'markers': {
        'radius': circle(radius, f'Radius {radius}', (255, 60, 60)),
        'generated-edge': circle(radius + 512, f'Generated edge {radius + 512}', (255, 255, 255)),
        'spawn': {'type': 'poi', 'label': 'Spawn', 'position': {'x': sx, 'y': sy, 'z': sz},
                  'detail': f'Spawn ({sx}, {sy}, {sz})'}}}}

    planned = anchors(log, radius)
    assert len(planned) == 47, f'{len(planned)} anchors in the log for radius {radius}'
    sets['anchors'] = {'label': f'Biome anchors ({len(planned)})', 'toggleable': True, 'sorting': 1, 'markers': {
        f'anchor-{b.split(":")[-1]}': {
            'type': 'shape', 'label': f'Anchor: {label(b)}', 'position': {'x': x, 'y': 64, 'z': z},
            'detail': f'Anchor {b}<br>centre ({x}, {z}), radius {r}',
            'shape': [{'x': x + p['x'], 'z': z + p['z']} for p in ring(r, 48)], 'shape-y': 64,
            'line-width': 2, 'line-color': {'r': 255, 'g': 200, 'b': 0, 'a': 1.0},
            'fill-color': {'r': 255, 'g': 200, 'b': 0, 'a': 0.15}, 'max-distance': 100000}
        for b, x, z, r in planned}}

    by_type = defaultdict(list)
    for sid, x, y, z in structure_starts(world): by_type[sid].append((x, y, z))
    for i, (sid, starts) in enumerate(sorted(by_type.items())):
        sets[f'structure-{sid.replace(":", "-")}'] = {
            'label': f'{label(sid)} ({len(starts)})', 'toggleable': True, 'default-hidden': len(starts) > 100,
            'sorting': 10 + i, 'markers': {
                f'{sid.split(":")[-1]}-{x}-{z}': {'type': 'poi', 'label': label(sid), 'position': {'x': x, 'y': y, 'z': z},
                                                  'detail': f'{sid}<br>({x}, {y}, {z})'}
                for x, y, z in starts}}
    return sets, {sid: len(s) for sid, s in sorted(by_type.items())}


if __name__ == '__main__':
    server, radius, log = sys.argv[1], int(sys.argv[2]), sys.argv[3]
    world = os.path.join(server, f'r{radius}')
    conf = os.path.join(server, 'config', 'bluemap', 'maps', f'r{radius}.conf')
    sets, counts = marker_sets(radius, world, log)
    text = open(conf, encoding='utf-8').read()
    head, sep, _ = text.rpartition('\nmarker-sets:')
    assert sep, f'no marker-sets block in {conf}'
    open(conf, 'w', encoding='utf-8').write(head + '\nmarker-sets: ' + json.dumps(sets, indent=1) + '\n')
    print('structures:', counts)
    print(f'wrote {sum(len(s["markers"]) for s in sets.values())} markers in {len(sets)} sets to {conf}')
