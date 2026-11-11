// No production functions are exported in Phase 3.
// Probe verifies emulator routing and default rejection; never accesses data.
if (process.env.FUNCTIONS_EMULATOR === "true") {
  const { onCall, HttpsError } = require("firebase-functions/v2/https");
  exports.foundationProbe = onCall({ region: "asia-southeast1" }, (request) => {
    if (!request.auth) throw new HttpsError("unauthenticated", "Authentication required.");
    throw new HttpsError("failed-precondition", "Phase 3 foundation: backend operations are not enabled.");
  });
}
