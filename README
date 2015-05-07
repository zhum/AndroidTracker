= Android phone tracker ==

=== Authors and License ===

This project is released under MIT License.
Autors: Sarimsakov Bakhrom Azimovich, Zhumatiy Sergey (serg@guru.ru)

=== Features ===

- tracking by GPS or Network
- store data on remote server
- active zones tracking and notifying by enter/leave
- mute time interval
- opensource!

=== How to install ===

==== Server ====

tracker.rb is the server, written in ruby using Sinatra framework. You can reimplement it in any language.
Protocol is simple:
POST / puts an point. Attributes are: 
  lat: float latitude
  long: float longitude
  time: unix time
  accuracy: float accuracy
  device: string device name

GET / gets list of poins in json format. Attributes are:
  start: unix time (get points from this time)
  end:   unix time (get points till this time, if ommited - till current time)
  limit: int limit of points number

To run server you need ruby, and bundler installed. Just run bundle install and if succeed, run ruby tracker.rb.
By default it starts listen on 0.0.0.0:4567.

==== Android programms ====

You need to create account at Android developers site, register your project and create google maps API key.
Place it at Android-tracker-supervisor/app/src/main/AndroidManifest.xml instead 'your-api-key-here'.
Load projects to android studion and compile. You may be asked for creation keystores, changing settings etc.

=== Use ===

Traget application must be run on target phone as you can mention. Supervisor app - on any device.
At first time supervisor device loads many track points and can 'hung'. Be patient and wait. After loading all points it will show track.
You can add or remove active zones and set notifying for each.

At first use set apropriate server address on both devices!

=== TODO ===

- add authorization
- add target device name changing
- add supervising of many devices
- zone tracking errors workarounds
- mobile network turn oni/off if needed

