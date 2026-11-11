import { readFileSync } from "node:fs";
import { after, before, test } from "node:test";
import assert from "node:assert/strict";
import { initializeTestEnvironment, assertFails, assertSucceeds } from "@firebase/rules-unit-testing";
import { initializeApp, deleteApp } from "firebase/app";
import { getAuth, connectAuthEmulator, signInAnonymously, signOut } from "firebase/auth";
import { getFunctions, connectFunctionsEmulator, httpsCallable } from "firebase/functions";
import { doc, collection, getDoc, getDocs, setDoc, updateDoc, deleteDoc, writeBatch } from "firebase/firestore";

const projectId = "demo-campusq-local"; // Firebase demo project: never a real deployment target.
let env;
before(async () => {
  assert.equal(process.env.GCLOUD_PROJECT, projectId);
  assert.ok(process.env.FIRESTORE_EMULATOR_HOST);
  env = await initializeTestEnvironment({
    projectId,
    firestore: { host: "127.0.0.1", port: 8080, rules: readFileSync("firestore.rules", "utf8") }
  });
  await env.withSecurityRulesDisabled(async context => {
    for (const name of collections) await setDoc(doc(context.firestore(), name, "fixture"), { ownerUid: "student", role: "STUDENT" });
  });
});
after(async () => { if (env) await env.cleanup(); });
const collections = ["users", "studentProfiles", "staffProfiles", "services", "queueTickets",
  "queueCounters", "appointments", "transactions", "notifications", "systemSettings", "auditLogs"];
const actors = [
  ["anonymous", null],
  ["student", { role: "STUDENT", email_verified: true }],
  ["staff", { role: "STAFF", email_verified: true }],
  ["administrator", { role: "ADMIN", email_verified: true }],
  ["forged-or-unverified", { role: "ADMIN", admin: true, email_verified: false }]
];
for (const [actor, claims] of actors) {
  for (const name of collections) {
    test(actor + " cannot read/list/create/update/delete " + name, async () => {
      const db = claims ? env.authenticatedContext(actor, claims).firestore() : env.unauthenticatedContext().firestore();
      await assertFails(getDoc(doc(db, name, "fixture")));
      await assertFails(getDocs(collection(db, name)));
      await assertFails(setDoc(doc(db, name, "new"), { ownerUid: actor, role: "ADMIN" }));
      await assertFails(updateDoc(doc(db, name, "fixture"), { role: "ADMIN", ownerUid: actor }));
      await assertFails(deleteDoc(doc(db, name, "fixture")));
    });
  }
}
test("existing document is present behind rules (positive harness control)", async () => {
  await env.withSecurityRulesDisabled(async context => {
    const result = await assertSucceeds(getDoc(doc(context.firestore(), "users", "fixture")));
    assert.equal(result.exists(), true);
  });
});
test("unknown and nested paths are closed", async () => {
  const db = env.authenticatedContext("administrator", { role: "ADMIN" }).firestore();
  for (const path of ["unknown/doc", "users/student/deviceTokens/device", "appointments/a/slotLocks/slot"]) {
    await assertFails(getDoc(doc(db, path)));
    await assertFails(setDoc(doc(db, path), { role: "ADMIN" }));
  }
});
test("atomic privilege escalation batch is rejected", async () => {
  const db = env.authenticatedContext("student", { role: "STUDENT" }).firestore();
  const batch = writeBatch(db);
  batch.set(doc(db, "users", "student"), { role: "ADMIN", isActive: true });
  batch.set(doc(db, "queueCounters", "service-day"), { nextSequence: 1 });
  await assertFails(batch.commit());
});
test("Auth emulator creates isolated identity; callable fails closed", async () => {
  const app = initializeApp({ projectId, apiKey: "local-emulator-only", appId: "1:000000000000:web:local" }, "foundation-test");
  try {
    const auth = getAuth(app);
    connectAuthEmulator(auth, "http://127.0.0.1:9099", { disableWarnings: true });
    const functions = getFunctions(app, "asia-southeast1");
    connectFunctionsEmulator(functions, "127.0.0.1", 5001);
    const probe = httpsCallable(functions, "foundationProbe");
    await assert.rejects(probe({}), { code: "functions/unauthenticated" });
    const credential = await signInAnonymously(auth);
    assert.ok(credential.user.uid);
    const token = await credential.user.getIdTokenResult();
    assert.equal(token.claims.role, undefined);
    await assert.rejects(probe({ role: "ADMIN" }), { code: "functions/failed-precondition" });
    await signOut(auth);
  } finally { await deleteApp(app); }
});
