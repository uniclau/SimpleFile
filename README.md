SimpleFile
==========

SimpleFile is a Cordova/Phonegap plugin that lets you read and write files from the filesystem.

You can also create/remove directories, extract the URI from a file and download remote content to a file.

It currently features support for **Android** and **iOS**. 

Install
-----

From the root of a cordova project run: 

	$ cordova plugin install https://github.com/uniclau/SimpleFile.git

Javascript API
-----

The plugin exposes the ```window.plugins.simpleFile``` object. It provides access to different **file systems** through their corresponding object:

* ```plugins.simpleFile.internal```
	* Private files stored in the app's data folder. The content will not be saved in iCloud
	* On iOS: ```/var/mobile/Applications/<UUID>/Library/NoCloud```
	* On Android: ```/data/data/<app-id>/files```
* ```plugins.simpleFile.internal```
	* Private files stored in the app's data folder. The content will be saved in iCloud.
	* On iOS: ```/var/mobile/Applications/<UUID>/Library```
	* On Android: ```/data/data/<app-id>/files```
* ```plugins.simpleFile.external```
	* On Android, it provides access to the device's SD card. It defaults to the private app storage if no SD card is not present in older devices. 
	* On iOS: ```/var/mobile/Applications/<UUID>/Library/NoCloud```
		* iPhones do not have external memory cards. Same as the ```internal``` filesystem.
	* On Android: ```<sdcard>/```
* ```plugins.simpleFile.bundle```
	* Files packaged in the app binary (i.e. ```www```)
	* This file system is read only

### Reading files

	window.plugins.simpleFile.<fs>.read(fileName, successCallback, errorCallback)

Reads the contents of a file and provides them to the given successCallback.

### Writing to a file
   
	window.plugins.simpleFile.<fs>.write(fileName, contents, successCallback, errorCallback)

Writes the contents in ```contents``` to the path provided in ```fileName```. This function will create any folders in ```fileName``` that do not exist yet. 

**Note:** This will not work if you use it with the ```bundle``` filesystem. 

### Removing a file or folder

	window.plugins.simpleFile.<fs>.remove(fileName, successCallback, errorCallback)

Removes the file or folder specified in ```fileName```. If ```fileName```is a **folder**, all of its contents are removed as well. 

**Note:** This will not work if you use it with the ```bundle``` filesystem. 
   
### Getting a file's URL
      
	window.plugins.simpleFile.<fs>.getURL(fileName, successCallback, errorCallback)

Provides the URL to reference a file from inside the browser. 
     
### Downloading remote files

	window.plugins.simpleFile.<fs>.download(url, fileName, successCallback, errorCallback)

Downloads a file and saves the contents to the file specified in ```fileName```.
This function will create any folders in ```fileName``` that do not exist yet. 

**Note:** This will not work if you use it with the ```bundle``` filesystem. 

### Creating a folder

	window.plugins.simpleFile.<fs>.createFolder(dirName, successCallback, errorCallback)

Creates a folder and all the parent directories that do not exist yet. 

**Note:** This will not work if you use it with the ```bundle``` filesystem. 

### Listing folders

	window.plugins.simpleFile.<fs>.list(folderName, successCallback, errorCallback)

Provides the ```successCallback``` function with an array of objects, containing the files and folders contained inside the ```folderName``` directory.

Every object in the array has the following structure:

	{
		name: "file.txt",
		isFolder: false
	}

To list the root directory, just set ```folderName``` to either ```""``` or ```"."```


### Copying files or folders
	
	window.plugins.simpleFile.copy(originFilesystem, originFileOrFolder, destinationFilesystem, destinationFileOrFolder, successCallback, errorCallback)

Copies the file or folder (with all its contents) to the given destination.

As an example:

	window.plugins.simpleFile.copy('bundle', 'logo.png', 'internal', 'logo.png', successCallback, errorCallback)

General example
---

The next example copies the phonegap logo to the internal directory and sets that logo.png as the current page of the webView:

	window.plugins.simpleFile.bundle.read("www/img/logo.png",function(data) {
	
		window.plugins.simpleFile.internal.write("logo.png", data, function() {
	
			window.plugins.simpleFile.internal.getUrl("logo.png", function(url) {
				alert(url);
				window.location.href = url;
			},function(err) {
				alert("ERROR in getUrl: "+err);
			});
		},function(err) {
			alert("ERROR: write: "+err);
		});
	},function(err) {
		alert("ERROR: read: "+err);
	});



