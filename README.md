# Exchange Rate Checker
This Android application interacts with the [FX Market API](https://fxmarketapi.com/) to fetch the exchange rate between two currencies.
It also displays the change in Exchange Rate for the past 30 days in a line graph.
A list of all previously searched Exchange Rates is also available.

## Components

* `Retrofit`, an HTTP client framework for Android, was used to interact with the FX Market API.
* `Room` Database was used to cache the available currencies as well as store all previous searched results.
* The `Pager 3` Library was used to paginate the list of historic searches as this list could grow indefinitely. 
* `Data binding` to reactively update the views when the underlying data changes.
* `Navigation Library` to easily navigate between fragments in a navigation graph.
* `Work Manager` to periodically check if new currencies are supported

## Architecture

The MVVM architecture was followed as closely as possible. 

The View is only responsible for setting
up the UI and observing changes to the underlying data

The Model consists of all data Entities and repositories used to access the data. The repository can 
either fetch data remotely through HTTP API requests or fetch data from the local database.

The View Model facilitates interactions between the View and the Model and holds any cached values
for easy retrieval.

# Known Shortcomings
The time series graph sometimes does not display labels on the X and Y axes.<br>
Time series data is not available for all currency combinations.<br>
The Exchange Rate is displayed but an amount conversion is not available.

# Building the project.

We use the Fx Market API. Add your `api key` inside the `main/secrets.xml` resource file and call it 
`api_key`. 
```xml
<string name="api_key">YOUR API KEY HERE</string>
```
Build the project and install the APK on any Android device supporting SDK 21+

An APK is available on this public Google Drive [Link](https://drive.google.com/file/d/1uSPOYkwQvl4z-3-aZWYZNnj__XC4_z_H/view?usp=sharing)


