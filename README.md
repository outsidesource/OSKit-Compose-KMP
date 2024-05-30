# OSKit Compose
An opinionated architecture/library for Jetbrains Compose Multiplatform development with an implementation of the [VISCE architecture](https://ryanmitchener.notion.site/VISCE-va-s-Architecture-d0878313b4154d2999bf3bf36cb072ff)

## Abstract
OSKit Compose is primarily a tool for us here at Outside Source. That being said, feel free to use this library in your own code.
We strive to adhere to semantic versioning.

## Contributions
Contributions are appreciated and welcome, but we are a small team and make no guarantees that your changes will be
implemented.

## Documentation
<https://outsidesource.github.io/OSKit-Compose-KMP/>

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
    * Asynchronous image loading
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

## Supported Platforms
Currently supported platforms include:
* Android
* JVM (MacOS/Windows/Linux)
* iOS

## Installation
```
implementation("com.outsidesource:oskit-compose:3.3.0")
```

## Example App
<https://github.com/outsidesource/OSKit-Example-App-KMP>

## Changelog
### 3.7.0 - 2024-05-30
#### Added
* onKeyEvent to BottomSheet
* KMPBackHandler/BackHandler to KMPPopup
### 3.6.0 - 2024-05-21
#### Added
* Focusable parameter to Popover
* KMPDisableScreenIdleTimeoutEffect
#### Breaking Changes
* Fixed typo in KMPTimePicker. Previously `TimeMerdiem` was `TimeMeridian`
### 3.5.0 - 2024-03-23
#### Added
* Support for Jetbrains Compose 1.6
* Support for Kotlin 1.9.23
#### Removed
* KMPResource - No longer works with compose 1.6
* KMPFont - No longer works with compose 1.6
* KMPImage - No longer works with compose 1.6
### 3.4.0 - 2024-03-06
#### Added
* KMPTimePicker
* KMPDatePicker
* KMPWheelPicker
### 3.3.0 - 2024-02-16
#### Added
* `rememberKMPWindowInfo()`
* `createInteractor()`
* `KMPAppLifecycleObserver`
* `Context` helper functions
* `rememberLastNonNullState()`
* Fallback locale for `KMPString`
* Markdown
  * Async image loading
  * Loading Markdown from other sources
* `PopupShapeCaretPosition`
#### Fixed
* iOS window insets issues
#### Breaking Changes
* Markdown `onLinkClick` now accepts a second parameter