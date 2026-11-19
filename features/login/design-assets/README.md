# Login icon sources

Original SVG icons downloaded from the Login Figma frame `10:2775` in
`jt6unsrw5ase8hVhZOWGdJ`, via its design-context asset URLs. Those temporary
URLs are not used by the app.

| Source | Figma node | Resource | Logical size |
|---|---|---|---|
| server.svg | 10:2852 | login_server.xml | 19 × 19 dp |
| username.svg | 10:2863 | login_username.xml | 19 × 19 dp |
| password.svg | 10:2872 | login_password.xml | 19 × 19 dp |
| visibility.svg | 10:2879 | login_visibility.xml | 19 × 19 dp |

All four originals have a 19 × 19 viewBox. Their runtime resources are XML vector
drawables with a 19 × 19 viewport and a 19 × 19 dp intrinsic size. The SVG path data,
1.425-unit stroke width, rounded caps and joins, and server icon's viewport clip are
preserved. Transparent fills and opaque black strokes supply an alpha mask;
Compose's `Icon` applies the theme tint in light and dark modes.

The app loads the XML resources through `painterResource` on Android, Desktop, JS
and WasmJS. No PNG variants are needed. The SVG originals remain the editable
source assets and are not bundled as application resources. When updating them,
copy each SVG path's `d` into `android:pathData`, retain its stroke attributes and
viewport clipping, and avoid references to Android theme attributes or resources.

The desktop sidebar illustration consists of native Figma layout shapes, not
these assets, and is implemented with Compose drawing/layout primitives.
