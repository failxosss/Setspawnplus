# SetSpawnPlus

Lehký plugin pro **Paper / Spigot 1.20+** (Java 17+): `/setspawn`, `/spawn`, **first spawn** pro nové hráče,
respawn na spawnu když hráč nemá postel, delay + cooldown a reload.

## Funkce
- `/setspawn`, `/spawn [hráč]` – klasický hlavní spawn
- `/setfirstspawn`, `/firstspawn` – samostatný spawn pro hráče, kteří jsou na serveru poprvé
- Po smrti respawn na spawnu, pokud hráč **nemá postel ani respawn anchor** (lze změnit, aby ignoroval i postel)
- Spawn pro každý svět zvlášť (`spawn.per-world`)
- Čekání před teleportem, cooldown, zrušení při pohybu / poškození
- Příkazy a broadcast při prvním připojení
- Všechny zprávy v `config.yml`, `/ssp reload` načte config i spawny bez restartu

## Příkazy a oprávnění
| Příkaz | Oprávnění | Výchozí |
|---|---|---|
| `/spawn` | `setspawn.use` | všichni |
| `/spawn <hráč>` | `setspawn.use.others` | op |
| `/setspawn` | `setspawn.set` | op |
| `/firstspawn` | `setspawn.firstspawn.use` | op |
| `/setfirstspawn` | `setspawn.firstspawn.set` | op |
| `/ssp reload`, `/ssp info` | `setspawn.reload` | op |
| – | `setspawn.bypass.delay`, `setspawn.bypass.cooldown` | op |

## Build
```
mvn package
```
Jar je v `target/`. Na GitHubu ho sestaví workflow v záložce **Actions**; při pushi tagu `v*` (např. `v1.0.0`) se jar přiloží k Releases.

## Instalace
Jar do složky `plugins/`, restart serveru, poté `/setspawn` a `/setfirstspawn`.
