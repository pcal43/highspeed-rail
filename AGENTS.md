# High-Speed Rail

High-Speed Rail is a Fabric mod that makes minecart transportation a viable
alternative to ice boats.

The mod increases minecart speeds far beyond vanilla limits while remaining
server-friendly and highly configurable. It supports both the classic and
experimental minecart physics systems and includes features such as:

- High-speed minecart travel
- Configurable acceleration and deceleration
- Per-block speed modifiers
- Automatic speed reduction on curves
- Optional speedometer
- Optional ice boat nerf

The project intentionally avoids adding new blocks or changing world data.
The goal is to improve transportation using existing vanilla mechanics.

## Design Philosophy

When making changes, prioritize:

1. Reliability over complexity.
2. Server-side compatibility.
3. Vanilla-friendly gameplay.
4. Configurability.
5. Performance on large multiplayer servers.

Avoid adding features that require client-side installation unless explicitly
requested.

Avoid adding new blocks, items, entities, or world-generation features unless
they clearly support the project's transportation goals.

## Repository Scope

This repository contains the source code for High-Speed Rail.

Only inspect files tracked by git.

Ignore:

- build/
- .gradle/
- run/
- logs/
- generated resources
- IDE metadata
- temporary files
- crash reports

Do not spend time analyzing generated or build output files.

## Cost-Aware Development

Repository-wide scans are expensive and should be avoided.

Before exploring the repository:

- Prefer targeted analysis.
- Read only files likely to be relevant.
- Start from files explicitly mentioned in the task.
- Follow references outward only as needed.
- Do not read entire directory trees unless necessary.

When discovering files, prefer:

```bash
git ls-files
```

When investigating a change, prefer:

```bash
git status
git diff
```

over broad repository searches.

## Coding Guidelines

Favor small, reviewable changes.

Avoid large-scale refactors unless explicitly requested.

Preserve existing behavior whenever possible.

When multiple implementations are possible:

- Choose the simplest solution.
- Minimize code churn.
- Minimize new dependencies.
- Preserve backwards compatibility.

## Minecraft-Specific Guidance

This project targets modern Minecraft versions using Fabric.

When updating between Minecraft versions:

- Prefer the smallest possible migration.
- Preserve existing configuration formats.
- Preserve existing gameplay behavior unless version changes require otherwise.
- Avoid speculative API migrations.

When fixing bugs:

- Fix the root cause rather than adding special cases.
- Keep configuration semantics stable.

## Working Style

Explain significant design decisions.

Call out potential compatibility risks.

If a task appears to require a broad repository scan, explain why before
proceeding.
