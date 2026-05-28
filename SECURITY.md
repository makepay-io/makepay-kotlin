# Security Policy

## Supported Versions

The `main` branch receives security fixes until versioned releases begin.

## Reporting a Vulnerability

Email security reports to `info@makepay.io`.

Please include affected methods, reproduction steps, expected impact, and any request or response examples with secrets removed.

## Secret Handling

- Keep `keySecret` and `webhookSecret` on a server or trusted backend.
- Do not ship MakePay partner credentials in Android, desktop, or browser apps.
- Verify webhook signatures against the exact raw body.
- Use environment variables or a managed secret store for production credentials.
