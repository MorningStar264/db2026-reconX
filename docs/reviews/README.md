# Config / Architecture Reviews

Each ticket that runs a Claude-assisted config review gets three files here,
named `TICKET-145.md`:

- `-prompt.md`   — exact prompt sent, files reviewed, constraints given
- `-findings.md` — Claude's raw findings table
- `-decisions.md`— team's Accept/Reject/Defer call per finding, with rationale

The PR for the ticket links all three in its description rather than
inlining them, so the history stays auditable independent of the PR itself.