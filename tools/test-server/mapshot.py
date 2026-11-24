"""Stitches a BlueMap map's low-res tiles into one top-view image (the web app's flat view, without a browser).

Usage: python mapshot.py <map-dir> <lod> <out> [--crop X0 Z0 X1 Z1] [--width PX]
  e.g. python tools/test-server/mapshot.py ../finite-test-server/bluemap/web/maps/r10000 2 r10000.jpg --width 1600

lod 1 is 1 block per pixel, each next lod 5 times coarser (BlueMap's default lodFactor 5). --crop takes block
coordinates; --width scales the result (nearest-neighbour when enlarging, so blocks stay sharp).
Each tile is a 501x1002 PNG: colour on top, height and light below, and one column/row of overlap.
"""
import argparse, glob, os, re
from PIL import Image

TILE = 500


def tiles(map_dir, lod):
    """{(tx, tz): path}. BlueMap splits long coordinates into one folder per digit: x-1/2/z3.png is (-12, 3)."""
    root = os.path.join(map_dir, 'tiles', str(lod))
    for path in glob.glob(os.path.join(root, '**', '*.png'), recursive=True):
        rel = os.path.relpath(path, root).replace(os.sep, '')[:-4]
        m = re.fullmatch(r'x(-?\d+)z(-?\d+)', rel)
        if m: yield (int(m[1]), int(m[2])), path


def stitch(map_dir, lod, crop=None):
    found = dict(tiles(map_dir, lod))
    assert found, f'no lod {lod} tiles in {map_dir}'
    f = 5 ** (lod - 1)  # blocks per pixel
    xs, zs = [t[0] for t in found], [t[1] for t in found]
    x0, z0 = min(xs) * TILE, min(zs) * TILE  # pixel origin, in units of f blocks
    img = Image.new('RGBA', ((max(xs) + 1) * TILE - x0, (max(zs) + 1) * TILE - z0), (8, 16, 28, 255))
    for (tx, tz), path in found.items():
        colour = Image.open(path).convert('RGBA').crop((0, 0, TILE, TILE))
        img.alpha_composite(colour, (tx * TILE - x0, tz * TILE - z0))
    if crop:
        bx0, bz0, bx1, bz1 = crop
        img = img.crop((bx0 // f - x0, bz0 // f - z0, bx1 // f - x0, bz1 // f - z0))
    return img.convert('RGB')


if __name__ == '__main__':
    p = argparse.ArgumentParser()
    p.add_argument('map_dir'); p.add_argument('lod', type=int); p.add_argument('out')
    p.add_argument('--crop', type=int, nargs=4, metavar=('X0', 'Z0', 'X1', 'Z1'))
    p.add_argument('--width', type=int)
    a = p.parse_args()
    img = stitch(a.map_dir, a.lod, a.crop)
    if a.width:
        h = round(img.height * a.width / img.width)
        img = img.resize((a.width, h), Image.NEAREST if a.width > img.width else Image.LANCZOS)
    img.save(a.out, quality=88)
    print(f'{a.out}: {img.width}x{img.height}')
