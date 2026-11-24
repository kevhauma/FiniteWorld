# Pixel-art icon: one round island with biome patches in an endless ocean, faint ring at the radius.
import math, random, sys
from PIL import Image
N = 32
random.seed(7)
C = (N - 1) / 2
# smooth value noise from a few random sinusoids
waves = [(random.uniform(1, 3), random.uniform(0, 6.3), random.uniform(.6, 1.4)) for _ in range(4)]
def coast(a):  # coastline radius at angle a: wanders inside the radius
    return 10.8 + sum(math.sin(a * f + p) * w * .8 for f, p, w in waves)
OCEAN, DEEP, RING = (38, 92, 168), (32, 80, 150), (74, 128, 196)
SAND, SAND2 = (219, 203, 140), (200, 184, 120)
BIOMES = [  # (centre angle in degrees, clockwise from north, colours)
    (0,   [(236, 242, 248), (206, 220, 232)]),  # snow
    (55,  [(64, 118, 72), (52, 100, 60)]),      # taiga
    (110, [(214, 196, 124), (196, 178, 110)]),  # desert
    (165, [(196, 104, 52), (168, 84, 44)]),     # badlands
    (225, [(70, 150, 42), (56, 128, 34)]),      # jungle
    (290, [(112, 168, 72), (96, 150, 60)]),     # plains
]
img = Image.new("RGB", (N, N))
for y in range(N):
    for x in range(N):
        dx, dy = x - C, y - C
        r = math.hypot(dx, dy)
        a = math.atan2(dx, -dy) % (2 * math.pi)
        cr = coast(a)
        if r <= cr - 1.1:
            deg = (math.degrees(a) + 14 * math.sin(r * .9 + a * 3)) % 360
            col = min(BIOMES, key=lambda b: min(abs(deg - b[0]), 360 - abs(deg - b[0])))[1]
            if r < 3.2: col = [(84, 156, 64), (70, 140, 52)]  # forest heart
            c = col[(x * 7 + y * 13) % 5 == 0]
        elif r <= cr:
            c = SAND if (x + y) % 4 else SAND2
        elif abs(r - 14.2) < .45 and int(math.degrees(a) / 12) % 2 == 0:
            c = RING  # dashed ring: the radius
        else:
            c = DEEP if (x * 5 + y * 3) % 17 == 0 else OCEAN
        img.putpixel((x, y), c)
for size, path in [(128, sys.argv[1]), (512, sys.argv[2])]:
    img.resize((size, size), Image.NEAREST).save(path)
