import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { authenticate } from './demoUsers.js';

describe('demo authentication', () => {
  it('authenticates admin users with admin role', () => {
    assert.deepEqual(authenticate('admin', 'admin123'), {
      username: 'admin',
      name: 'Avery Stone',
      role: 'ADMIN'
    });
  });

  it('authenticates purchase users with user role', () => {
    assert.deepEqual(authenticate('user1', 'user123'), {
      username: 'user1',
      name: 'Jordan Lee',
      role: 'USER'
    });
  });

  it('rejects invalid credentials', () => {
    assert.equal(authenticate('admin', 'wrong'), null);
  });
});
