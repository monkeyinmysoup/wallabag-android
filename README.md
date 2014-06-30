# [Wallabag][] client for Android. 
[![Build Status](https://travis-ci.org/monkeyinmysoup/wallabag-android.svg?branch=develop-studio)](https://travis-ci.org/monkeyinmysoup/wallabag-android)

This project is an Android app for reading and submitting articles to Wallabag (formerly Poche).

This app is based on the excellent work of [Erik Pires' fork][0] of the [original app][1], adding:

- a new design (this'll be somewhat of an ongoing thing),
- code clean-up and refactoring to work with Android Studio,
- app thumbnails in the list of articles.

![wallabag screenshot](https://i.imgur.com/MVretvH.png)


My other goals for this fork are to: 
- improve the code further,
- add support to submit articles to wallabag without being redirected to the website,
- add support for tagging articles after 'wallabagging' them (much like Pocket does),
- improve syncing of read/unread status,
- improve security.

Most of those features are probably dependent on the development of an API in the upcoming version 2 of the [wallabag-v2][] backend as well.


## Licenses

This application is released under GPL and uses the following libraries and projects:

- Chris Banes' [FloatLabelLayout][4], licensed under the [Apache 2.0 license][apache]
- Pixplicity's [EasyPrefs][5] (Apache license 2.0),
- Qbus' [Cupboard][6] (Apache License 2.0),
- Square's [OkHttp][7] (Apache License 2.0),
- and contains a style initially generated by the Action Bar Style Generator (Apache License 2.0)

[wallabag]: http://wallabag.org
[wallabag-v2]: https://github.com/wallabag/wallabag/tree/v2-silex
[apache]: http://www.apache.org/licenses/LICENSE-2.0.txt
[0]: https://github.com/erickpires/wallabag-android
[1]: https://github.com/wallabag/android-app
[2]: https://github.com/castorflex/SmoothProgressBar
[3]: http://pt.wikipedia.org/wiki/Beerware
[4]: https://gist.github.com/chrisbanes/11247418
[5]: https://github.com/Pixplicity/EasyPreferences
[6]: https://bitbucket.org/qbusict/cupboard
[7]: https://square.github.io/okhttp/