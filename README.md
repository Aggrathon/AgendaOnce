# Agenda Once

An android app and (more importantly) widget that shows a
calendar agenda view where multiday events only occurs *once*,
not one listing per day.

## Download

The latest release can be found [here](https://github.com/Aggrathon/AgendaOnce/releases/).  
The app only needs one permission, read calendar.

## Features

 - Calendar agenda where multiday events are only shown once.
 - Widget with the same agenda view.
 - Touching events in the agenda opens them in you prefered calendar app.
 - Widget uses calendar broadcasts instead of periodical updates for increased battery life.
 - Respects the visibilty setting for different calendars.

### Note

This app is designed with battery efficiency in mind. 
So the agenda is only updated when you open the app.
The widgets, on the otherhand, are updated using broadcasts
(when a calendar is changed and when an event ends).
The broadcasts are controlled by Android and might be
a bit delayed (by even a couple of minutes) due to
Androids power optimisations.
