# KUATO fork notes

Fork of Gathin23/cleanyourtv (launcher 3.3, base commit e62aa10a) for Raymond's NVIDIA Shield (Plex info on the home screen).
Branch `kuato`; remotes: `origin` = rcisneros138/cleanyourtv, `upstream` = Gathin23/cleanyourtv. Package stays `com.gathin.tvhome`.

## Build / install (on hermes)

    HOST_KEY_DIR=$HOME/.cleanyourtv-key tools/docker-build.sh launcher testReleaseUnitTest assembleRelease
    adb -s 192.168.86.239:5555 install -r launcher/app/build/outputs/apk/release/app-release.apk
    adb -s 192.168.86.239:5555 shell 'am force-stop com.gathin.tvhome; am start -W -a android.intent.action.MAIN -c android.intent.category.HOME -n com.gathin.tvhome/.MainActivity'

Signing key: `~/.cleanyourtv-key/debug.keystore` (never commit). It is from el-jefe's Docker volume
`cleanyourtv-gradle-amd64` (`/g/.android/debug.keystore`; the plain `cleanyourtv-gradle` volume has no key). Backup on
el-jefe: `~/claude-tv/cleanyourtv-debug.keystore`. SHA-256 of the cert, identical to the installed APK's signer:
`7B:F5:28:4B:5F:97:07:76:DF:3B:CB:79:0D:A0:55:D7:21:C9:C9:35:3E:79:BD:F5:ED:7D:58:38:6A:59:67:56`.
If `pm install` reports a signature mismatch, stop; never uninstall the launcher.

## Merging upstream

    git fetch upstream && git merge upstream/main   # merge, never rebase

Upstream files are touched only at hooks marked `// kuato:`; new code lives in `launcher/app/src/main/java/com/gathin/tvhome/kuato/`.

## Baseline (stock launcher + Raymond's config, 2026-11-01, Shield, 5 runs)

- `am start -W` HOME from inside Plex, TotalTime ms: 59, 54, 57, 55, 58 (mean 56.6). Warm start; the launcher process was already alive.
- Jank: `dumpsys gfxinfo com.gathin.tvhome`, 20 cycles of RIGHT,RIGHT,DOWN,LEFT,LEFT,UP sent one `input keyevent` at a time (about 1 minute per run, not 10 s;
  each adb key spawn is slow). `gfxinfo reset` does not clear counters for the shell user, so numbers are cumulative since the process started.
  Janky frames after each of 5 runs: 0 (0.00%) every time; total frames 5130, 5139, 5139, 5139, 5140 (the launcher draws only on change);
  p50 5 ms, p90 5 ms, p99 6 ms, 0 missed vsync, 0 slow UI thread.

## Plex link formats (Plex 2026.20.0 on the Shield, server machineIdentifier eb4899df...9022)

Encoded server URI: `server%3A%2F%2F<machineId>%2Fcom.plexapp.plugins.library%2Flibrary%2Fmetadata%2F<ratingKey>`;
fire with `am start -a android.intent.action.VIEW -d '<uri>' com.plexapp.android`.

- WORKS, plays: `plex://watch/video?uri=<enc>` for a movie (19728) or an episode (19697; played Love is Blind).
  Playback takes 10 to 16 s to start (spinner first), so wait before judging. A cold Plex (just force-stopped) showed "Can't Reach Server / Failed to fetch play queue" at 13 s; start Plex first or wait longer.
- `plex://watch/video|show|details?uri=<enc show key 19691>` also START PLAYBACK of the show (resume episode). They do not open the show page.
- Do NOT open the show page: `plex://show?uri=`, `plex://preplay?uri=`, `plex://details?uri=` (all land on Plex Home or the Discover/Live TV start page);
  `https://watch.plex.tv/show/love-is-blind` ("Uh Oh... Something's Not Right"); `plex://show/<guid>` (error); `plex://show/<guid>?server=<id>` (error);
  `plex://watch/show/<guid>` (spinner, nothing); `plex://library/metadata/19691`, `plex://metadata/19691` (Home).
- Winning show-page URI: **none found** (see task report for the full list).
