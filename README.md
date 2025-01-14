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
    * rememberLastNonNullValue()
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
### 3.7.2 - 2024-08-30
#### Fixed
* Non-fullscreen popups (i.e. Popover) on Android had a regression where they were not positioned correctly
### 3.7.1 - 2024-07-12
#### Fixed
* Back handling and key events in Android Popups 
### 3.7.0 - 2024-05-30
#### Added
* onKeyEvent to BottomSheet
* KmpBackHandler/BackHandler to KmpPopup
### 3.6.0 - 2024-05-21
#### Added
* Focusable parameter to Popover
* KmpDisableScreenIdleTimeoutEffect
#### Breaking Changes
* Fixed typo in KmpTimePicker. Previously `TimeMerdiem` was `TimeMeridian`
### 3.5.0 - 2024-03-23
#### Added
* Support for Jetbrains Compose 1.6
* Support for Kotlin 1.9.23
#### Removed
* KmpResource - No longer works with compose 1.6
* KmpFont - No longer works with compose 1.6
* KmpImage - No longer works with compose 1.6
### 3.4.0 - 2024-03-06
#### Added
* KmpTimePicker
* KmpDatePicker
* KmpWheelPicker
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