import random, os
from PIL import Image, ImageDraw

ROOT = "/home/claude/madworld"
TEX = f"{ROOT}/src/main/resources/assets/madworld/textures"
JAVA = f"{ROOT}/src/main/java/com/madworld/client/MadModels.java"
SIZE = 128

def hexc(h):
    h = h.lstrip('#')
    if len(h) == 3: h = ''.join(ch*2 for ch in h)
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4)) + (255,)
def shade(c, k):
    return tuple(max(0, min(255, int(v * k))) for v in c[:3]) + (255,)

# part: (name, parent, pivot, cuboid(x,y,z,w,h,d), color, ops)
# ops: (face, x, y, w, h, color)
def P(name, parent, pivot, box, color, ops=()):
    return dict(name=name, parent=parent, pivot=pivot, box=box, color=color, ops=list(ops))

MOBS = {}

MOBS["creeper_courier"] = ("courier", [
    P("head", None, (0,6,0), (-4,-8,-4,8,8,8), "#5fb85a",
      [("front",1,2,2,2,"#111"),("front",5,2,2,2,"#111"),("front",3,4,2,3,"#111"),("front",2,5,1,2,"#111"),("front",5,5,1,2,"#111")]),
    P("cap", "head", (0,0,0), (-5,-10,-5,10,3,10), "#c0392b", [("top",3,3,4,4,"#f5f5f5")]),
    P("visor", "head", (0,0,0), (-4,-8,-7,8,1,3), "#7b241c"),
    P("body", None, (0,6,0), (-4,0,-2,8,12,4), "#4fa34c"),
    P("bag", "body", (0,0,0), (-4,1,2,8,8,4), "#8d5a2b", [("back",0,3,8,2,"#f5deb3"),("back",3,0,2,8,"#f5deb3")]),
    P("leg0", None, (-2,18,4), (-2,0,-2,4,6,4), "#3f8a3d"),
    P("leg1", None, (2,18,4), (-2,0,-2,4,6,4), "#3f8a3d"),
    P("leg2", None, (-2,18,-4), (-2,0,-2,4,6,4), "#3f8a3d"),
    P("leg3", None, (2,18,-4), (-2,0,-2,4,6,4), "#3f8a3d"),
])

PINK = "#f2a2b0"
MOBS["pig_emperor"] = ("boss", [
    P("head", None, (0,12,-8), (-4,-4,-6,8,8,6), PINK,
      [("front",1,3,2,2,"#2b2b2b"),("front",5,3,2,2,"#2b2b2b"),("front",1,3,1,1,"#ffffff"),("front",5,3,1,1,"#ffffff")]),
    P("snout", "head", (0,0,0), (-2,0,-7,4,3,1), "#e0788f", [("front",1,1,1,1,"#7a2a3a"),("front",2,1,1,1,"#7a2a3a")]),
    P("ear_l", "head", (0,0,0), (-4,-6,-2,2,2,1), "#e0788f"),
    P("ear_r", "head", (0,0,0), (2,-6,-2,2,2,1), "#e0788f"),
    P("crown", "head", (0,0,0), (-4,-6,-6,8,2,6), "#ffd700", [("front",1,0,6,1,"#fff3a0")]),
    P("spike1", "head", (0,0,0), (-4,-8,-6,1,2,1), "#ffd700"),
    P("spike2", "head", (0,0,0), (3,-8,-6,1,2,1), "#ffd700"),
    P("spike3", "head", (0,0,0), (-4,-8,-1,1,2,1), "#ffd700"),
    P("spike4", "head", (0,0,0), (3,-8,-1,1,2,1), "#ffd700"),
    P("spike5", "head", (0,0,0), (-1,-9,-4,2,3,2), "#ffd700", [("front",0,1,2,1,"#e53935")]),
    P("body", None, (0,13,0), (-5,-5,-8,10,10,16), PINK,
      [("left",0,4,16,1,"#ffd700"),("right",0,4,16,1,"#ffd700")]),
    P("cape", "body", (0,0,0), (-5,-5,8,10,11,1), "#b71c1c",
      [("back",0,0,10,1,"#ffd700"),("back",0,10,10,1,"#ffd700"),("back",0,0,1,11,"#ffd700"),("back",9,0,1,11,"#ffd700")]),
    P("leg0", None, (-3.5,18,5), (-2,0,-2,4,6,4), "#e8909f"),
    P("leg1", None, (3.5,18,5), (-2,0,-2,4,6,4), "#e8909f"),
    P("leg2", None, (-3.5,18,-5), (-2,0,-2,4,6,4), "#e8909f"),
    P("leg3", None, (3.5,18,-5), (-2,0,-2,4,6,4), "#e8909f"),
])

MOBS["zombie_dancer"] = ("biped", [
    P("head", None, (0,0,0), (-4,-8,-4,8,8,8), "#6aa84f", [("front",0,3,8,2,"#111111"),("front",2,6,4,1,"#222222")]),
    P("afro", "head", (0,0,0), (-5,-12,-5,10,5,10), "#c2185b"),
    P("body", None, (0,0,0), (-4,0,-2,8,12,4), "#f1c40f", [("front",0,4,8,1,"#e67e22"),("front",0,8,8,1,"#e67e22")]),
    P("arm_r", None, (-5,2,0), (-3,-2,-2,4,12,4), "#f1c40f"),
    P("arm_l", None, (5,2,0), (-1,-2,-2,4,12,4), "#f1c40f"),
    P("leg_r", None, (-1.9,12,0), (-2,0,-2,4,12,4), "#d63384"),
    P("leg_l", None, (1.9,12,0), (-2,0,-2,4,12,4), "#d63384"),
])

MOBS["village_madman"] = ("biped", [
    P("head", None, (0,0,0), (-4,-10,-4,8,10,8), "#d9a07a",
      [("front",0,3,3,3,"#ffffff"),("front",1,4,1,1,"#111111"),("front",4,2,4,4,"#ffffff"),("front",6,4,1,1,"#111111"),
       ("front",0,2,8,1,"#3b2a1a"),("front",2,8,4,1,"#5a2a2a")]),
    P("nose", "head", (0,0,0), (-1,-3,-6,2,4,2), "#c98a63"),
    P("hat1", "head", (0,0,0), (-5,-12,-5,10,2,10), "#cfd8dc"),
    P("hat2", "head", (0,0,0), (-3,-16,-3,6,4,6), "#b0bec5"),
    P("body", None, (0,0,0), (-4,0,-3,8,12,6), "#8b5a2b", [("front",0,7,8,1,"#4e342e")]),
    P("arm_r", None, (-5,2,0), (-3,-2,-2,4,10,4), "#8b5a2b"),
    P("arm_l", None, (5,2,0), (-1,-2,-2,4,10,4), "#8b5a2b"),
    P("leg_r", None, (-2,12,0), (-2,0,-2,4,12,4), "#4e342e"),
    P("leg_l", None, (2,12,0), (-2,0,-2,4,12,4), "#4e342e"),
])

MOBS["ninja_chicken"] = ("bird", [
    P("body", None, (0,16,0), (-3,-3,-4,6,6,8), "#1c1c24"),
    P("head", None, (0,13,-4), (-2,-6,-2,4,6,3), "#1c1c24", [("front",0,2,1,1,"#ffffff"),("front",3,2,1,1,"#ffffff")]),
    P("beak", "head", (0,0,0), (-1,-3,-4,2,2,2), "#ff9800"),
    P("band", "head", (0,0,0), (-3,-5,-3,6,2,5), "#e53935"),
    P("tails", "head", (0,0,0), (-1,-5,2,2,1,5), "#e53935"),
    P("leg_r", None, (-1.5,19,1), (-1,0,-1,2,5,2), "#ff9800"),
    P("leg_l", None, (1.5,19,1), (-1,0,-1,2,5,2), "#ff9800"),
    P("wing_r", None, (-3.5,14,0), (-1,0,-3,1,4,6), "#2a2a35"),
    P("wing_l", None, (3.5,14,0), (0,0,-3,1,4,6), "#2a2a35"),
])

MOBS["elite_skeleton"] = ("biped", [
    P("head", None, (0,0,0), (-4,-8,-4,8,8,8), "#e8e8e0",
      [("front",1,3,2,2,"#111111"),("front",5,3,2,2,"#111111"),("front",2,6,4,1,"#111111"),("front",5,2,3,1,"#d4af37")]),
    P("helmet", "head", (0,0,0), (-5,-9,-5,10,4,10), "#d4af37"),
    P("plume", "head", (0,0,0), (-1,-12,-4,2,3,8), "#c62828"),
    P("body", None, (0,0,0), (-4,0,-2,8,12,4), "#e8e8e0", [("front",1,2,6,1,"#9e9e90"),("front",1,5,6,1,"#9e9e90"),("front",1,8,6,1,"#9e9e90")]),
    P("plate", "body", (0,0,0), (-5,0,-3,10,8,6), "#d4af37", [("front",4,0,2,8,"#b8922a")]),
    P("arm_r", None, (-5,2,0), (-1,-2,-1,2,12,2), "#e8e8e0"),
    P("arm_l", None, (5,2,0), (-1,-2,-1,2,12,2), "#e8e8e0"),
    P("leg_r", None, (-2,12,0), (-1,0,-1,2,12,2), "#e8e8e0"),
    P("leg_l", None, (2,12,0), (-1,0,-1,2,12,2), "#e8e8e0"),
])

def pack(parts):
    items = []
    for p in parts:
        x,y,z,w,h,d = p["box"]
        items.append((2*(w+d), d+h, p))
    items.sort(key=lambda t: -t[1])
    cx = cy = shelf = 0
    for fw, fh, p in items:
        if cx + fw > SIZE:
            cx = 0; cy += shelf; shelf = 0
        p["uv"] = (cx, cy)
        cx += fw; shelf = max(shelf, fh)
    assert cy + shelf <= SIZE, "texture overflow"

def faces(u, v, w, h, d):
    return {
        "top": (u+d, v, w, d), "bottom": (u+d+w, v, w, d),
        "right": (u, v+d, d, h), "front": (u+d, v+d, w, h),
        "left": (u+d+w, v+d, d, h), "back": (u+2*d+w, v+d, w, h),
    }

def paint(mobname, parts, rnd):
    img = Image.new("RGBA", (SIZE, SIZE), (0,0,0,0))
    px = img.load()
    for p in parts:
        x,y,z,w,h,d = p["box"]; u,v = p["uv"]
        base = hexc(p["color"]); fs = faces(u,v,w,h,d)
        for fname,(fx,fy,fw,fh) in fs.items():
            k = {"top":1.12,"bottom":0.8,"front":1.0,"back":0.92,"left":0.95,"right":0.95}[fname]
            for ix in range(fw):
                for iy in range(fh):
                    n = rnd.uniform(0.93, 1.07)
                    px[fx+ix, fy+iy] = shade(base, k*n)
        for (fname, ox, oy, ow, oh, col) in p["ops"]:
            fx,fy,fw,fh = fs[fname]
            for ix in range(ow):
                for iy in range(oh):
                    if 0 <= ox+ix < fw and 0 <= oy+iy < fh:
                        px[fx+ox+ix, fy+oy+iy] = hexc(col)
    img.save(f"{TEX}/entity/{mobname}.png")

def f(x): 
    x = float(x); return (str(int(x)) if x == int(x) else str(x)) + "f"

def gen_java():
    out = []
    out.append("""package com.madworld.client;

import com.madworld.MadWorldMod;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;

/** AUTO-GENERATED by tools/gen_assets.py — geometry matches textures/entity/*.png */
public final class MadModels {
    private MadModels() {}
""")
    for name in MOBS:
        out.append(f'    public static final EntityModelLayer {name.upper()} = new EntityModelLayer(MadWorldMod.id("{name}"), "main");')
    out.append("")
    for name,(kind,parts) in MOBS.items():
        pack(parts)
        out.append(f"    public static TexturedModelData {name}() {{")
        out.append("        ModelData md = new ModelData();")
        out.append("        ModelPartData root = md.getRoot();")
        for p in parts:
            x,y,z,w,h,d = p["box"]; u,v = p["uv"]; px,py,pz = p["pivot"]
            parent = "root" if p["parent"] is None else "v_" + p["parent"]
            out.append(f'        ModelPartData v_{p["name"]} = {parent}.addChild("{p["name"]}", ModelPartBuilder.create().uv({u}, {v}).cuboid({f(x)}, {f(y)}, {f(z)}, {f(w)}, {f(h)}, {f(d)}), ModelTransform.of({f(px)}, {f(py)}, {f(pz)}, 0f, 0f, 0f));')
        out.append(f"        return TexturedModelData.of(md, {SIZE}, {SIZE});")
        out.append("    }\n")
    out.append("}")
    open(JAVA, "w", encoding="utf-8").write("\n".join(out) + "\n")

# ---------- item sprites ----------
def outline(img, col=(30,20,20,255)):
    w,h = img.size; src = img.load(); res = img.copy(); dst = res.load()
    for x in range(w):
        for y in range(h):
            if src[x,y][3] == 0:
                for dx,dy in ((1,0),(-1,0),(0,1),(0,-1)):
                    nx,ny = x+dx,y+dy
                    if 0<=nx<w and 0<=ny<h and src[nx,ny][3] > 0:
                        dst[x,y] = col; break
    return res

def sprite(name, fn):
    img = Image.new("RGBA", (16,16), (0,0,0,0)); d = ImageDraw.Draw(img); fn(d)
    outline(img).save(f"{TEX}/item/{name}.png")

def banana(d):
    d.polygon([(3,11),(5,13),(9,13),(12,10),(13,5),(11,3),(11,6),(9,9),(6,10)], fill=(250,215,40,255))
    d.line([(3,11),(6,10),(9,9),(11,6)], fill=(255,240,120,255))
    d.rectangle([11,2,12,3], fill=(110,80,30,255)); d.point((13,6), fill=(110,80,30,255))
def banana_peel(d):
    d.polygon([(2,12),(5,9),(8,11),(11,8),(14,11),(10,13),(7,13),(4,14)], fill=(245,205,35,255))
    d.polygon([(6,10),(8,5),(10,10)], fill=(225,185,25,255)); d.line([(8,5),(8,9)], fill=(120,90,30,255))
def remote(d):
    d.rectangle([4,2,11,14], fill=(70,70,80,255)); d.rectangle([5,3,10,6], fill=(120,220,140,255))
    d.ellipse([6,8,9,11], fill=(230,40,40,255)); d.rectangle([5,12,6,13], fill=(200,200,60,255)); d.rectangle([9,12,10,13], fill=(60,120,220,255))
    d.line([(8,2),(8,0)], fill=(180,180,190,255))
def hammer(d):
    d.line([(3,13),(10,6)], fill=(130,85,40,255), width=2)
    d.rectangle([8,1,14,6], fill=(255,210,50,255)); d.rectangle([9,2,13,3], fill=(255,245,150,255))
    d.polygon([(8,1),(14,1),(14,6),(8,6)], outline=(190,140,20,255))
def soup(d):
    d.ellipse([1,7,14,14], fill=(140,90,50,255)); d.ellipse([2,6,13,11], fill=(245,150,30,255))
    d.ellipse([4,7,8,9], fill=(255,210,90,255)); d.line([(10,2),(12,5)], fill=(200,230,255,255)); d.line([(6,1),(7,4)], fill=(200,230,255,255))
    d.line([(9,8),(12,8)], fill=(120,200,60,255))
def dice(d):
    d.rectangle([2,2,13,13], fill=(245,245,245,255))
    for p in [(4,4),(4,11),(11,4),(11,11),(7,7),(8,8)]: d.rectangle([p[0],p[1],p[0]+1,p[1]+1], fill=(200,30,30,255))
def horn(d):
    d.polygon([(1,6),(9,3),(14,2),(14,6),(9,10),(1,10)], fill=(230,215,170,255))
    d.polygon([(1,6),(1,10),(4,10),(4,6)], fill=(170,120,60,255)); d.line([(14,2),(14,6)], fill=(255,60,60,255))
    d.line([(5,11),(8,14)], fill=(170,120,60,255)); d.point((6,6), fill=(255,255,255,255))
def tpstick(d):
    d.line([(3,13),(11,5)], fill=(120,70,160,255), width=2); d.rectangle([10,1,13,4], fill=(230,120,255,255))
    d.point((11,0), fill=(255,255,255,255)); d.point((14,2), fill=(255,255,255,255)); d.point((9,2), fill=(255,255,255,255))
    d.rectangle([11,2,12,3], fill=(255,255,255,255))
def toilet_item(d):
    d.rectangle([3,1,12,6], fill=(255,215,40,255)); d.rectangle([2,7,13,10], fill=(255,230,90,255))
    d.rectangle([4,11,11,14], fill=(230,185,25,255)); d.rectangle([4,8,11,9], fill=(255,250,200,255)); d.rectangle([5,2,10,3], fill=(255,245,160,255))
def magnet(d):
    d.arc([2,2,13,14], 180, 360, fill=(220,40,40,255), width=3)
    d.rectangle([2,7,4,12], fill=(220,40,40,255)); d.rectangle([11,7,13,12], fill=(220,40,40,255))
    d.rectangle([2,11,4,13], fill=(210,210,220,255)); d.rectangle([11,11,13,13], fill=(210,210,220,255)); d.point((7,0), fill=(255,230,80,255))
def potato_forbidden(d):
    d.ellipse([2,4,13,13], fill=(120,60,150,255)); d.ellipse([4,5,8,7], fill=(160,100,190,255))
    d.rectangle([5,7,6,8], fill=(20,20,20,255)); d.rectangle([9,7,10,8], fill=(20,20,20,255)); d.rectangle([6,10,9,10], fill=(20,20,20,255))
    d.point((7,2), fill=(255,40,40,255)); d.point((8,1), fill=(255,40,40,255))
def potato_golden(d):
    d.ellipse([2,4,13,13], fill=(255,205,40,255)); d.ellipse([4,5,8,7], fill=(255,245,150,255))
    d.point((10,9), fill=(190,140,20,255)); d.point((5,10), fill=(190,140,20,255)); d.point((9,6), fill=(190,140,20,255))
def crown(d):
    d.polygon([(2,12),(2,5),(5,8),(8,3),(11,8),(14,5),(14,12)], fill=(255,215,40,255))
    d.rectangle([2,10,13,12], fill=(235,185,25,255)); d.rectangle([7,6,8,7], fill=(230,40,60,255)); d.point((3,6), fill=(60,120,255,255)); d.point((13,6), fill=(60,200,90,255))

SPRITES = dict(banana_grenade=banana, banana_peel=banana_peel, chaos_remote=remote, hammer_of_fate=hammer, speed_soup=soup,
               lucky_die=dice, apocalypse_horn=horn, teleport_stick=tpstick, golden_toilet=toilet_item, pig_magnet=magnet,
               forbidden_potato=potato_forbidden, golden_potato=potato_golden, emperor_crown=crown)

def block_tex():
    rnd = random.Random(7)
    img = Image.new("RGBA", (16,16))
    px = img.load()
    for x in range(16):
        for y in range(16):
            n = rnd.uniform(0.9, 1.08)
            px[x,y] = shade((255,205,40,255), n)
    d = ImageDraw.Draw(img)
    d.rectangle([0,0,15,15], outline=(190,140,20,255))
    for p in [(3,3),(11,4),(6,10),(12,12)]: d.point(p, fill=(255,250,190,255))
    img.save(f"{TEX}/block/golden_toilet.png")

def icon():
    img = Image.new("RGBA", (128,128), (40,10,60,255)); d = ImageDraw.Draw(img)
    d.ellipse([24,34,104,110], fill=(242,162,176,255))
    d.rectangle([40,34,88,50], fill=(255,215,0,255))
    for x in (40,56,72,84): d.polygon([(x,34),(x+4,18),(x+8,34)], fill=(255,215,0,255))
    d.ellipse([46,64,58,76], fill=(20,20,20,255)); d.ellipse([70,64,82,76], fill=(20,20,20,255))
    d.ellipse([50,84,78,102], fill=(224,120,143,255)); d.rectangle([58,90,61,96], fill=(120,40,60,255)); d.rectangle([67,90,70,96], fill=(120,40,60,255))
    img.save(f"{ROOT}/src/main/resources/assets/madworld/icon.png")

if __name__ == "__main__":
    for n in ("entity","item","block"): os.makedirs(f"{TEX}/{n}", exist_ok=True)
    for i,(name,(kind,parts)) in enumerate(MOBS.items()):
        pack(parts); paint(name, parts, random.Random(100+i))
    gen_java()
    for n,fn in SPRITES.items(): sprite(n, fn)
    block_tex(); icon()
    print("generated", len(MOBS), "mobs,", len(SPRITES), "sprites")
