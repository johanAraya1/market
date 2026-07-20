# Skill Registry — Market

**Generated**: 2026-07-19
**Project**: Market
**Mode**: openspec

## Indexed Skills

| Skill | Trigger | Scope | Path |
|-------|---------|-------|------|
| branch-pr | creating, opening, or preparing PRs for review | user | `~/.config/opencode/skills/branch-pr/SKILL.md` |
| chained-pr | PRs over 400 lines, stacked PRs, review slices | user | `~/.config/opencode/skills/chained-pr/SKILL.md` |
| cognitive-doc-design | writing guides, READMEs, RFCs, onboarding, architecture | user | `~/.config/opencode/skills/cognitive-doc-design/SKILL.md` |
| comment-writer | PR feedback, issue replies, reviews, Slack messages | user | `~/.config/opencode/skills/comment-writer/SKILL.md` |
| find-skills | "how do I do X", "find a skill for X", extend capabilities | user | `~/.agents/skills/find-skills/SKILL.md` |
| go-testing | Go tests, go test coverage, Bubbletea teatest | user | `~/.config/opencode/skills/go-testing/SKILL.md` |
| issue-creation | creating GitHub issues, bug reports, feature requests | user | `~/.config/opencode/skills/issue-creation/SKILL.md` |
| judgment-day | judgment day, dual review, adversarial review | user | `~/.config/opencode/skills/judgment-day/SKILL.md` |
| skill-creator | new skills, agent instructions, AI usage patterns | user | `~/.config/opencode/skills/skill-creator/SKILL.md` |
| skill-improver | improve skills, audit skills, refactor skills | user | `~/.config/opencode/skills/skill-improver/SKILL.md` |
| work-unit-commits | implementation, commit splitting, chained PRs | user | `~/.config/opencode/skills/work-unit-commits/SKILL.md` |

## SDD Pipeline Skills (excluded from index)

`sdd-init`, `sdd-explore`, `sdd-propose`, `sdd-spec`, `sdd-design`, `sdd-tasks`, `sdd-apply`, `sdd-verify`, `sdd-archive`, `sdd-onboard`, `_shared`, `skill-registry` — managed internally by the SDD orchestrator.

## Conventions

No project-level convention files found (`AGENTS.md`, `.cursorrules`, `CLAUDE.md`, etc.).

## Usage

Pass exact `SKILL.md` paths to subagents. This registry is an index — read `SKILL.md` source files for full instructions.
