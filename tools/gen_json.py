import json, os
R = "/home/claude/madworld/src/main/resources"
A = f"{R}/assets/madworld"; D = f"{R}/data/madworld"
def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, "w", encoding="utf-8").write(json.dumps(obj, ensure_ascii=False, indent=2) + "\n")

ITEMS = ["banana_grenade","banana_peel","chaos_remote","hammer_of_fate","speed_soup","lucky_die","apocalypse_horn",
         "teleport_stick","pig_magnet","forbidden_potato","golden_potato","emperor_crown"]
for i in ITEMS:
    w(f"{A}/items/{i}.json", {"model": {"type": "minecraft:model", "model": f"madworld:item/{i}"}})
    w(f"{A}/models/item/{i}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"madworld:item/{i}"}})
# hammer / stick held like tools
for i in ("hammer_of_fate","teleport_stick"):
    w(f"{A}/models/item/{i}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"madworld:item/{i}"}})

# golden toilet
w(f"{A}/items/golden_toilet.json", {"model": {"type": "minecraft:model", "model": "madworld:block/golden_toilet"}})
tex = {"0": "madworld:block/golden_toilet", "particle": "madworld:block/golden_toilet"}
def el(frm, to):
    return {"from": frm, "to": to, "faces": {f: {"texture": "#0"} for f in ("north","east","south","west","up","down")}}
w(f"{A}/models/block/golden_toilet.json", {
    "textures": tex,
    "elements": [el([5,0,4],[11,4,12]), el([3,4,2],[13,8,12]), el([2,8,2],[14,9,12]), el([4,9,11],[12,16,14]), el([7,15,10],[9,16,11])],
    "display": {
        "gui": {"rotation": [30, 225, 0], "scale": [0.62, 0.62, 0.62]},
        "ground": {"scale": [0.4, 0.4, 0.4]},
        "fixed": {"scale": [0.5, 0.5, 0.5]},
        "thirdperson_righthand": {"scale": [0.4, 0.4, 0.4]},
        "firstperson_righthand": {"scale": [0.4, 0.4, 0.4]}
    }})
w(f"{A}/blockstates/golden_toilet.json", {"variants": {
    "facing=north": {"model": "madworld:block/golden_toilet"},
    "facing=east": {"model": "madworld:block/golden_toilet", "y": 90},
    "facing=south": {"model": "madworld:block/golden_toilet", "y": 180},
    "facing=west": {"model": "madworld:block/golden_toilet", "y": 270}}})
w(f"{D}/loot_table/blocks/golden_toilet.json", {"type": "minecraft:block", "pools": [{"rolls": 1,
    "entries": [{"type": "minecraft:item", "name": "madworld:golden_toilet"}],
    "conditions": [{"condition": "minecraft:survives_explosion"}]}],
    "random_sequence": "madworld:blocks/golden_toilet"})
w(f"{R}/data/minecraft/tags/block/mineable/pickaxe.json", {"values": ["madworld:golden_toilet"]})

# lang
ru = {
 "itemGroup.madworld": "МИР СОШЁЛ С УМА!",
 "item.madworld.banana_grenade": "Банановая граната", "item.madworld.banana_peel": "Банановая кожура",
 "item.madworld.chaos_remote": "Пульт хаоса", "item.madworld.hammer_of_fate": "Молоток судьбы",
 "item.madworld.speed_soup": "Суп скорости", "item.madworld.lucky_die": "Кубик удачи",
 "item.madworld.apocalypse_horn": "Рог апокалипсиса", "item.madworld.teleport_stick": "Палка телепортации",
 "item.madworld.pig_magnet": "Магнит для свиней", "item.madworld.forbidden_potato": "Запрещённая картошка",
 "item.madworld.golden_potato": "Золотая картофелина", "item.madworld.emperor_crown": "Корона Свиного Императора",
 "block.madworld.golden_toilet": "Золотой унитаз",
 "entity.madworld.creeper_courier": "Крипер-курьер", "entity.madworld.pig_emperor": "Свиной Император",
 "entity.madworld.zombie_dancer": "Зомби-танцор", "entity.madworld.village_madman": "Деревенский сумасшедший",
 "entity.madworld.ninja_chicken": "Курица-ниндзя", "entity.madworld.elite_skeleton": "Элитный скелет",
 "entity.madworld.banana_grenade": "Банановая граната", "entity.madworld.golden_potato": "Золотая картофелина"
}
en = {
 "itemGroup.madworld": "THE WORLD WENT MAD!",
 "item.madworld.banana_grenade": "Banana Grenade", "item.madworld.banana_peel": "Banana Peel",
 "item.madworld.chaos_remote": "Chaos Remote", "item.madworld.hammer_of_fate": "Hammer of Fate",
 "item.madworld.speed_soup": "Speed Soup", "item.madworld.lucky_die": "Lucky Die",
 "item.madworld.apocalypse_horn": "Apocalypse Horn", "item.madworld.teleport_stick": "Teleport Stick",
 "item.madworld.pig_magnet": "Pig Magnet", "item.madworld.forbidden_potato": "Forbidden Potato",
 "item.madworld.golden_potato": "Golden Potato", "item.madworld.emperor_crown": "Pig Emperor's Crown",
 "block.madworld.golden_toilet": "Golden Toilet",
 "entity.madworld.creeper_courier": "Creeper Courier", "entity.madworld.pig_emperor": "Pig Emperor",
 "entity.madworld.zombie_dancer": "Zombie Dancer", "entity.madworld.village_madman": "Village Madman",
 "entity.madworld.ninja_chicken": "Ninja Chicken", "entity.madworld.elite_skeleton": "Elite Skeleton",
 "entity.madworld.banana_grenade": "Banana Grenade", "entity.madworld.golden_potato": "Golden Potato"
}
w(f"{A}/lang/ru_ru.json", ru); w(f"{A}/lang/en_us.json", en)

# advancements (all granted from code through the "trigger" criterion)
def adv(name, icon, title, desc, frame="task", parent="madworld:root", hidden=False, bg=None):
    d = {"display": {"icon": {"id": icon}, "title": {"text": title}, "description": {"text": desc},
                     "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": hidden},
         "criteria": {"trigger": {"trigger": "minecraft:impossible"}}, "requirements": [["trigger"]]}
    if parent: d["parent"] = parent
    if bg: d["display"]["background"] = bg
    w(f"{D}/advancement/{name}.json", d)
adv("root", "madworld:chaos_remote", "МИР СОШЁЛ С УМА!", "Добро пожаловать. Назад дороги нет.", parent=None, bg="minecraft:gui/advancements/backgrounds/adventure")
adv("just_wanted_wood", "minecraft:oak_log", "Я просто хотел добыть дерево", "Срубить дерево — и сразу попасть в хаос.")
adv("chicken_knows", "minecraft:feather", "Курица знает слишком много", "Курица-ниндзя что-то у тебя украла.")
adv("is_this_minecraft", "madworld:lucky_die", "Это точно Minecraft?", "Безумие мира достигло половины.", frame="goal")
adv("pig_king", "madworld:emperor_crown", "Король свиней", "Победить Свиного Императора.", frame="challenge")
adv("potato_changed_life", "madworld:forbidden_potato", "Картошка изменила мою жизнь", "Съесть Запрещённую картошку.")
adv("survived_chaos", "madworld:apocalypse_horn", "Пережил полный хаос", "Пережить 10 безумных событий.", frame="challenge")
adv("secret_flush", "madworld:golden_toilet", "Слив королей", "Спустить воду в Золотом унитазе.", frame="goal", hidden=True)
print("json ok")
