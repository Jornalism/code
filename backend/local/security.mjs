import { promisify } from 'node:util';
const derive = promisify(scrypt);
const parameters = { N: 32768, r: 8, p: 3, maxmem: 64 * 1024 * 1024 };
export const digest = value => createHash('sha256').update(value).digest('hex');
export const newToken = () => randomBytes(32).toString('base64url');
export async function passwordHash(password) {
  const salt = randomBytes(16).toString('hex');
  const key = await derive(password, salt, 64, parameters);
  return 'scrypt$32768$8$3$' + salt + '$' + key.toString('hex');
}

export class ApiError extends Error {
  constructor(status, code, message) { super(message); this.status=status; this.code=code; }
}
export function password(value) {
  requireValue(typeof value === 'string' && value.length >= 8 && Buffer.byteLength(value) <= 256,'Use a password of 8–256 bytes.');
  return value;
}
export function fields(body, allowed) {
  requireValue(body && typeof body === 'object' && !Array.isArray(body) && Object.keys(body).every(k=>allowed.includes(k)), 'Unsupported request fields.');
}
