# Login icon sources

Original SVG icons downloaded from the Login Figma frame `10:2775` in
`jt6unsrw5ase8hVhZOWGdJ`, via its design-context asset URLs. Those temporary
URLs are not used by the app.

| Source | Figma node | Resource | Logical size |
|---|---|---|---|
| server.svg | 10:2852 | login_server.png | 19 × 19 dp |
| username.svg | 10:2863 | login_username.png | 19 × 19 dp |
| password.svg | 10:2872 | login_password.png | 19 × 19 dp |
| visibility.svg | 10:2879 | login_visibility.png | 19 × 19 dp |

All four originals have a 19 × 19 viewBox. The matching PNG resources are
rasterized at 4× (76 × 76) using librsvg for compatibility with all four Compose
resource targets, including Android. Shapes and proportions are preserved;
Compose's `Icon` applies theme tints. To regenerate from the originals:

```sh
rsvg-convert --width 76 --height 76 server.svg -o ../src/commonMain/composeResources/drawable/login_server.png
```

The desktop sidebar illustration consists of native Figma layout shapes, not
these assets, and is implemented with Compose drawing/layout primitives.
