import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { money } from './formatters.js';

describe('money', () => {
  it('formats numeric values with two decimal places', () => {
    assert.equal(money(12), '12.00');
    assert.equal(money(12.5), '12.50');
  });

  it('treats empty values as zero', () => {
    assert.equal(money(), '0.00');
    assert.equal(money(null), '0.00');
  });
});
