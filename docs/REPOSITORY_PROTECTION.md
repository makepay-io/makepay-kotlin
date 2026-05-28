# Repository Protection

The intended repository baseline is:

- Public repository under `makecryptoio`
- Default branch: `main`
- Squash merge enabled
- Merge commits and rebase merges disabled
- Delete branch on merge enabled
- Dependabot alerts and security updates enabled
- GitHub Actions workflow permissions set to read-only by default
- Branch protection on `main`
- Required Maven validation check
- One approving review before merge
- Stale review dismissal enabled
- Linear history required
- Force pushes and branch deletion disabled
- Conversation resolution required

This file mirrors the repository settings so future maintainers can audit drift.
