# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.3] - 2026-11-08

### Fixed

- Accounts are now identified by server URL **and** API token. Connecting the
  same server URL with a different token adds a separate saved account instead
  of overwriting the existing one.
- Switching and removing accounts respects the exact server/token pair, so
  accounts sharing a URL stay independent.

## [1.0.2] - 2026-11-08

### Added

- Saved accounts: multiple Linkding servers and API tokens can be stored on the
  device and switched from **Settings → Accounts**.
- **Add account** flow in Settings to connect another server without losing the
  current one.
- Existing configured servers are migrated into the saved account list
  automatically.

### Changed

- **Disconnect** now forgets only the active account and switches to another
  saved account when one is available.
- Activity and About screen report version `1.0.2`.

## [1.0.0] - 2026-11-08

### Added

- Initial release.
- Browse, search, filter, sort, and paginate Linkding bookmarks.
- Add and edit bookmarks with tags, notes, and unread/shared/archived state.
- Open, share, copy, archive, delete, and mark bookmarks read or unread.
- Receive links from the Android share sheet.
- Browse and search tags.
- Four Catppuccin flavors and fourteen accent colors with live preview.
- Optional favicons and self-signed certificate support.

[1.0.3]: https://github.com/arifnurrohim/linkding-client/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/arifnurrohim/linkding-client/compare/v1.0.0...v1.0.2
[1.0.0]: https://github.com/arifnurrohim/linkding-client/releases/tag/v1.0.0
