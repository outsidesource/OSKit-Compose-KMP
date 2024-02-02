# OSKit Compose
An opinionated architecture/library for Compose Multiplatform development

# Demo
<https://github.com/outsidesource/OSKit-Example-App-KMP>

## Abstract
OSKit Compose is primarily a tool for us here at Outside Source. That being said, feel free to use this library in your own code.
We strive to adhere to semantic versioning.

## Contributions
Contributions are appreciated and welcome, but we are a small team and make no guarantees that your changes will be
implemented.

## Supported Platforms
Currently supported platforms include:
* Android
* JVM (MacOS/Windows/Linux)
* iOS

## Feature Highlights
* Common
  * Animation
    * TransitionAnimatedContent (AnimatedContent with flexible transitions)
    * Common CubicBezier transitions
  * Canvas
    * Blur
    * URL Image painter
    * Bitmap Painter
    * Painter to Bitmap converter
  * Layouts
    * Arrangement.spacedByWithPadding()
    * FixedTableRow
    * FlexRow
    * PanAndScale
    * WrappableRow
  * Popups (able to be placed anywhere in composable tree)
    * BottomSheet
    * Drawer
    * Modal
    * Popover
    * Popup
  * OSKit Integration
    * Router
      * Compose implementation of OSKit Router
      * Backpress handling
      * Route transitions
    * Interactor
      * Compose implementation of OSKit Interactor
  * Markdown
    * Markdown renderer
    * Customizable styles with sane defaults
    * Load local or remote images and specify alignment and size
  * Modifier
    * borderTop, borderBottom, borderStart, borderEnd
    * innerShadow, outerShadow
    * disablePointerInput, preventClickPropagationToParent
    * Desktop external drag and drop support
  * Resources
    * Fonts
    * Images
    * Strings with localization
  * System UI
    * Application Lifecycle Observer
    * Customizable Window Insets
    * Customizable system bar colors
    * Window info
  * Misc
    * rememberLastNonNullState()
* Android
  * Context helpers
* Desktop
  * Persisted window state
  * SizedWindow for windows with enforced minimum size

## Changelog