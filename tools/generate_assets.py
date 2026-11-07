#!/usr/bin/env python3
"""Generate original, deterministic Minecraft workbench and pixel GUI assets.
Source-controlled procedure avoids distributing third-party artwork or binary libraries.
"""
from pathlib import Path
from PIL import Image, ImageDraw
import json, random

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/"src/main/resources/assets/gundb"
BLOCK=RES/"textures/block";ITEM=RES/"textures/item"
GUI=RES/"textures/gui/container";MODELS=RES/"models/block"
for d in [BLOCK,ITEM,GUI,MODELS]:d.mkdir(parents=True,exist_ok=True)
random.seed(20261107)
# 16-color industrial palette; each 64x64 atlas tile has visible pixel texture.
colors=["#303a43","#56616b","#859098","#b5c1c2","#111b26","#252e34",
        "#d9ab30","#3e454a","#67452a","#b57543","#2b606d","#57b8ba",
        "#7a3431","#d95b40","#78786b","#18242b"]
atlas=Image.new("RGB",(256,256))
a=ImageDraw.Draw(atlas)
for i,color in enumerate(colors):
    x=(i%4)*64;y=(i//4)*64
    a.rectangle((x,y,x+63,y+63),fill=color)
    for k in range(75):
        px=x+random.randrange(64);py=y+random.randrange(64)
        a.point((px,py),fill=random.choice(["#4f5862","#263139","#717a7e",color]))
    if i==6:
        for p in range(-64,125,18):
            a.polygon([(x+p,y),(x+p+8,y),(x+p+72,y+64),(x+p+64,y+64)],fill="#171c21")
    if i==7:
        for xx in range(x+5,x+64,16):
            for yy in range(y+5,y+64,16):
                a.rectangle((xx,yy,xx+5,yy+5),fill="#101820")
atlas.save(BLOCK/"repair_station_atlas.png",optimize=True)
def tile(i):return [(i%4)*4,(i//4)*4,(i%4)*4+4,(i//4)*4+4]
elems=[]
def cube(x0,y0,z0,x1,y1,z1,mat,top=None):
    faces={}
    for side in ["north","south","east","west","up","down"]:
        faces[side]={"texture":"#atlas","uv":tile(top if side=="up" and top is not None else mat)}
    elems.append({"from":[x0,y0,z0],"to":[x1,y1,z1],"faces":faces})
# One-block base with raised pegboard, heavy table, tool storage and indicator lights.
for x in [1,13]:
    for z in [1,13]:
        cube(x,0,z,x+2,12,z+2,0,2)
        cube(x-.25,0,z-.25,x+2.25,.8,z+2.25,4)
for z in [2,12.8]:
    cube(2.5,2.2,z,13.5,3,z+.8,1)
cube(0,10.8,0,16,13.2,16,0,2)
cube(1,13.2,1,15,13.45,15,5,1)
cube(2,13.46,2,11.2,13.52,12,10,10)
cube(2.6,3,3,8.6,7.5,8.5,14,1)
cube(2.4,7.5,2.9,8.8,8.2,8.7,1,2)
cube(10,3,3,13.5,5.5,8.5,1,2)
cube(4,7.5,.1,12,10.1,.7,1)
cube(6.5,8.3,0,10,8.7,.5,3)
for x in [3,5,11,13]:
    cube(x,11.8,.2,x+.45,12.6,.8,2)
cube(1.1,12.9,14.3,2.1,29.5,15.6,0)
cube(13.9,12.9,14.3,14.9,29.5,15.6,0)
cube(2,17,14.45,14,28.7,15.55,7)
cube(1.5,28.3,14.1,14.5,29.5,16,2)
for x in [3.2,5.2,7.2,9.2,11.2,13.2]:
    for y in [19.2,21.8,24.4,27]:
        cube(x,y,14.35,x+.30,y+.30,14.45,4)
# Side tool silhouettes and industrial top rail.
cube(10.3,13.55,4,14.7,13.8,11.5,4,2)
cube(10.5,13.85,6,12.5,14.1,9.5,3)
cube(3,13.6,14.2,13,14.3,14.8,6)
for x in [2.8,5.2,8,11.5,13.5]:
    cube(x,12.7,.15,x+.6,13.1,.55,6)
for i in range(4):
    cube(14.9,6+i*1.2,4,15.4,6.4+i*1.2,5.5,11 if i%2 else 6)
# 3D tools anchored to backboard.
cube(4.0,21,14.1,4.55,26.5,14.6,9)
cube(3.2,25.8,13.9,5.4,27.4,14.8,2)
cube(10.5,20.5,14.1,11,26.5,14.6,8)
cube(10,20.5,13.8,11.5,21.4,14.7,3)
cube(11,20.5,13.8,12.5,21.4,14.7,3)
json.dump({"parent":"minecraft:block/block","textures":{"atlas":"gundb:block/repair_station_atlas","particle":"gundb:block/repair_station_atlas"},"elements":elems},
          (MODELS/"gun_repair_station.json").open("w",encoding="utf-8"),separators=(",",":"))
# Voxel screwdriver icon: yellow-black grip, dark steel shaft, bright steel tip.
im=Image.new("RGBA",(32,32),(0,0,0,0));d=ImageDraw.Draw(im)
d.rectangle((6,16,17,21),fill="#171d23");d.rectangle((7,17,15,20),fill="#d7a536")
for x in [9,12,15]:d.rectangle((x,17,x+1,19),fill="#2c3139")
d.rectangle((17,18,26,19),fill="#707e86");d.rectangle((26,18,29,19),fill="#c6d4d3")
d.rectangle((6,17,7,20),fill="#e5cc63")
im.save(ITEM/"screwdriver.png")
# Game GUI is texture-only; localized labels and avatar render at runtime.
gui=Image.new("RGBA",(320,256),(0,0,0,0));g=ImageDraw.Draw(gui)
def R(a,b,c,d,color):g.rectangle((a,b,c,d),fill=color)
R(5,6,314,230,"#141a20");R(2,2,311,228,"#434b51")
R(8,8,307,224,"#8c9598");R(13,13,303,221,"#d0d1cb")
R(21,13,300,34,"#353c44");R(97,10,225,34,"#212a31")
for x in list(range(25,91,16))+list(range(242,296,16)):
    g.polygon([(x,17),(x+9,17),(x,31),(x-9,31)],fill="#d5a22e")
R(18,40,301,129,"#505960");R(23,44,296,123,"#c9cac4")
for x0,x1 in [(26,124),(130,224),(235,292)]:
    R(x0,45,x1,58,"#343b43");R(x0+2,46,x1-2,48,"#667077")
for x in [70,160,260]:
    R(x-5,59,x+21,85,"#272f37");R(x-2,62,x+18,82,"#606970")
    R(x,64,x+15,79,"#9c9d99")
for x0,x1 in [(27,127),(133,234),(241,290)]:
    R(x0,96,x1,120,"#353d44");R(x0+2,98,x1-2,118,"#56606a")
R(18,132,301,136,"#444e54")
R(24,137,295,224,"#bec0ba")
for row in range(3):
    for col in range(9):
        x=80+18*col;y=151+18*row
        R(x-1,y-1,x+16,y+16,"#3b4449");R(x,y,x+15,y+15,"#969894")
for col in range(9):
    x=80+18*col;y=209
    R(x-1,y-1,x+16,y+16,"#3b4449");R(x,y,x+15,y+15,"#969894")
for x in [10,294]:
    for y in [42,124,210]:
        R(x,y,x+5,y+5,"#353b43");R(x+1,y+1,x+3,y+3,"#b9c0be")
for x in range(21,300,14):
    g.polygon([(x,225),(x+8,225),(x+4,229),(x-4,229)],fill="#d1a52d")
# Small blocky hammer and screwdriver decorations stay outside interaction slots.
R(280,7,299,12,"#2a333c");R(281,8,294,10,"#c0c7c9")
R(291,13,295,31,"#9b6439")
R(281,118,299,121,"#c74f32");R(269,119,281,120,"#859198")
gui.save(GUI/"gun_repair_station.png")
print("Generated",len(elems),"3D elements and pixel-styled GUI")
