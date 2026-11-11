// Run from project root, inside Firebase emulators:exec only.
const { spawnSync } = require("node:child_process");
if (process.env.GCLOUD_PROJECT !== "demo-campusq-local" || !process.env.FIRESTORE_EMULATOR_HOST) {
  throw new Error("Refusing to run outside the explicit local demo Emulator Suite.");
}
const options = { stdio: "inherit", env: process.env };
let result = spawnSync(process.execPath,
  ["--test", "--test-concurrency=1", "backend/tests/foundation.test.mjs"], options);
if (result.status !== 0) process.exit(result.status || 1);
result = spawnSync("cmd.exe", ["/d", "/c", ".\\gradlew.bat",
  ":app:testDebugUnitTest", ":app:assembleDebug", ":app:lintDebug", ":app:connectedDebugAndroidTest",
  "-Pandroid.testInstrumentationRunnerArguments.firebaseEmulators=true", "--max-workers=2", "--console=plain"], options);
if (result.error) console.error(result.error);
process.exit(result.status || (result.error ? 1 : 0));
