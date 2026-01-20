# Custom Fonts

Place your brand font files (.ttf or .otf) in this directory.

## Naming Convention

Use lowercase names with underscores:

- `brand_regular.ttf`
- `brand_medium.ttf`
- `brand_semibold.ttf`
- `brand_bold.ttf`

## Example Structure

```
font/
├── brand_regular.ttf
├── brand_medium.ttf
├── brand_semibold.ttf
├── brand_bold.ttf
└── README.md
```

## Usage

After adding fonts, they are automatically accessible via generated `Res.font.*` references.
See `theme/Typography.kt` for implementation.

