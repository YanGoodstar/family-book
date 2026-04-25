const assert = require('node:assert');
const brand = require('../utils/brand');

const expectedKeys = [
  'BRAND_DESCRIPTION',
  'BRAND_EXPLAINER',
  'BRAND_LOGO',
  'BRAND_NAME',
  'BRAND_SHORT_SLOGAN',
  'BRAND_SLOGAN'
].sort();

const actualKeys = Object.keys(brand).sort();
assert.deepStrictEqual(
  actualKeys,
  expectedKeys,
  'Brand utilities should only export the six shared constants.'
);

assert.strictEqual(brand.BRAND_NAME, '朵攒攒', 'BRAND_NAME should match the shared brand name.');
assert.strictEqual(
  brand.BRAND_EXPLAINER,
  '朵攒攒，寓意把一点点积累，慢慢攒成会开花的未来。',
  'BRAND_EXPLAINER should match the brand explainer text.'
);
assert.strictEqual(
  brand.BRAND_SLOGAN,
  '把每一次小积累，慢慢攒成一朵花。',
  'BRAND_SLOGAN should match the brand slogan.'
);
assert.strictEqual(
  brand.BRAND_SHORT_SLOGAN,
  '今天攒一点，未来开一朵。',
  'BRAND_SHORT_SLOGAN should match the shorter slogan.'
);
assert.strictEqual(
  brand.BRAND_DESCRIPTION,
  '朵攒攒是一款帮助你为梦想慢慢积累的小程序，记录每一次存下的钱，看见目标一点点开花。',
  'BRAND_DESCRIPTION should match the detailed description.'
);
assert.strictEqual(
  brand.BRAND_LOGO,
  '/assets/images/brand/duozanzan-avatar.png',
  'BRAND_LOGO should match the avatar path.'
);

console.log('brand-metadata.test.js passed');
