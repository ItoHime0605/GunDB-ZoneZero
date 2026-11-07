#!/usr/bin/env python3
"""Compose the user's independent station overlay with author-published GunDB 2.2.2 sources.

The upstream repository is checked out separately; it is not presented as our original work.
Preserve all original gun damage/jam stats; alter only repair item selection.
"""
import json, shutil
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
UP=ROOT/"upstream"
OUR=ROOT/"src/main"
THEIR=UP/"src/main"
assert (THEIR/"java/mod/cdv/gdb/GunDurability.java").is_file(), "Expected author 2.2.2 source tree"

# Add our authored Java classes while keeping original files untouched.
for f in (OUR/"java").rglob("*.java"):
    out=THEIR/"java"/f.relative_to(OUR/"java")
    assert not out.exists(), "Refusing to overwrite original class: "+str(out)
    out.parent.mkdir(parents=True,exist_ok=True)
    shutil.copy2(f,out)

# Merge translations. Never discard author-provided existing keys.
for f in (OUR/"resources").rglob("*"):
    if not f.is_file(): continue
    rel=f.relative_to(OUR/"resources")
    if rel.as_posix().startswith("assets/gundb/lang/"): continue
    out=THEIR/"resources"/rel
    out.parent.mkdir(parents=True,exist_ok=True)
    assert not out.exists(), "Refusing to overwrite original resource: "+str(out)
    shutil.copy2(f,out)

for lang in ("zh_tw","en_us"):
    original=THEIR/"resources/assets/gundb/lang"/(lang+".json")
    added=OUR/"resources/assets/gundb/lang"/(lang+".json")
    a=json.loads(original.read_text(encoding="utf-8")) if original.exists() else {}
    b=json.loads(added.read_text(encoding="utf-8"))
    a.update(b)
    original.parent.mkdir(parents=True,exist_ok=True)
    original.write_text(json.dumps(a,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")

# Remove GunDB's old anvil Mixin, and its misleading JEI anvil repair recipes.
conf=THEIR/"resources/gundb.mixins.json"
mix=json.loads(conf.read_text(encoding="utf-8"))
assert "AnvilMenuMixin" in mix["mixins"], "Upstream anvil registration changed: review patch"
mix["mixins"].remove("AnvilMenuMixin")
# The old inspect hook targets a signature changed by TaCZ 1.2.0.
# Keep it disabled pending a verified replacement.
mix["mixins"]=[s for s in mix["mixins"] if s!="InspectKeybindHook"]
mix["mixins"].append("RepairStationModMixin")
conf.write_text(json.dumps(mix,indent=2)+"\n",encoding="utf-8")
legacy=THEIR/"java/mod/cdv/gdb/jei/JeiRepairPlugin.java"
if legacy.exists(): legacy.unlink()

# Preserve base GunDB's per-category and per-gun durability/damage/jamming stats.
# Only repair-item choice is redirected to the new screwdriver.
base=THEIR/"resources/data/gundb/gundb"
changed=0
for p in base.rglob("*.json"):
    item=json.loads(p.read_text(encoding="utf-8"))
    if "RepairItem" in item:
        item["RepairItem"]="gundb:screwdriver"
        p.write_text(json.dumps(item,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
        changed+=1
# DMR was not represented in the official 2.2.2 default data.
if not (base/"dmr.json").exists():
    d=json.loads((base/"rifle.json").read_text(encoding="utf-8"))
    (base/"dmr.json").write_text(json.dumps(d,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
print("Integrated independent GunDB station; configured",changed,"original categories / guns")

# Compile against the exact TaCZ NeoForge 1.21.1 1.2.0 API used on ZeroZone.
# The original author project ships a 1.1.8 compile dependency; replace it in the
# temporary checkout only. Do not vendor third-party binary JARs in our public repo.
build=UP/"build.gradle"
gradle=build.read_text(encoding="utf-8")
old='implementation files("libs/tacz-neoforge-1.21.1-1.1.8-hotfix-r3.jar")'
assert old in gradle, "Upstream changed TaCZ dependency; review replacement before compiling"
gradle=gradle.replace(old,'implementation "curse.maven:timeless-and-classics-zero-1028108:9454361"')
build.write_text(gradle,encoding="utf-8")
properties=UP/"gradle.properties"
prop=properties.read_text(encoding="utf-8")
assert "mod_version=2.2.2" in prop, "Unexpected upstream mod version"
prop=prop.replace("mod_version=2.2.2","mod_version=2.2.2-ZoneZero.0.7.2")
properties.write_text(prop,encoding="utf-8")
print("TaCZ 1.2.0 compile dependency configured via Curse Maven file 9454361")
