const express = require('express');
const path = require('path');
const app = express();
const PORT = 5000;

app.use(express.static('public'));
app.use(express.json());

app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.get('/api/project', (req, res) => {
  res.json({
    name: 'FormFit AI',
    version: '1.0.0',
    platform: 'Android',
    minSdk: 26,
    targetSdk: 34,
    kotlinVersion: '1.9.22',
    modules: ['app', 'core', 'vision', 'ui'],
    features: [
      'Real-time pose detection (MediaPipe)',
      'Rep counting state machine',
      'Skeleton overlay with color feedback',
      'Multi-exercise support',
      'Custom routines',
      'Supabase Auth (Google, Email, Magic Link)',
      'Stripe subscription plans',
      'Progress dashboard',
      'Onboarding survey',
      'GPU Delegate / NNAPI acceleration'
    ]
  });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`FormFit AI project server running on port ${PORT}`);
});
