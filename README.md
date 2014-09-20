SimpleFile
==========

SimpleFile is a Cordova/Phonegap plugin that lets you read and write files from the app's private local filesysten.

You can also create/remove directories, extract the URI from a file and download remote files.

It currently features support for Android devices, but iOS is on the road. 

Javascript API
-----

The Simple plugin has an object for each fileSystems ( <fs> )
Currently, available fs arr:

- internal:  Private files available only to the app.
- external: Public file systems
- bundle: Files contained in the bundle/assets (read only)

### Reading files

	window.plugins.simpleFile.<fs>.read(fileName, successCallback, errorCallback)

Reads the contents of a file and provides them to the given successCallback.

### Writing to a file
   
	window.plugins.simpleFile.<fs>.write(fileName, contents, successCallback, errorCallback)

Writes the contents in ```contents``` to the path provided in ```fileName```. This function will create any folders in ```fileName``` that do not exist yet. 

### Removing a file or folder

	window.plugins.simpleFile.<fs>.remove(fileName, successCallback, errorCallback)

Removes the file or folder specified in ```fileName```. If ```fileName```is a **folder**, all of its contents are removed as well. 
   
### Getting a file's URL
      
	window.plugins.simpleFile.<fs>.getURL(fileName, successCallback, errorCallback)

Provides the URL to reference a file from inside the browser. 
     
### Downloading remote files

	window.plugins.simpleFile.<fs>.download(url, fileName, successCallback, errorCallback)

Downloads a file and saves the contents to the file specified in ```fileName```.
This function will create any folders in ```fileName``` that do not exist yet. 

### Creating a folder

	window.plugins.simpleFile.<fs>.createFolder(dirName, successCallback, errorCallback)

Creates a folder and all the parent directories that do not exist yet. 

### Listing a folder's elements

	window.plugins.simpleFile.<fs>.list(folderName, successCallback, errorCallback)

Provides the ```successCallback``` function with an array of objects, containing the files and folders contained inside the ```folderName``` directory.

Every object in the array has the following structure:

	{
		name: "file.txt",
		isFolder: false
	}

To list the root directory, just set ```folderName``` to either ```""``` or ```"."```

Example
---

The next example copies the phonegap logo to the internal directory sets that logo as the current page:

window.plugins.simpleFile.bundle.read("www/img/logo.png",function(data) {
 	window.plugins.simpleFile.internal.write("logo.png", data, function() {
 		window.plugins.simpleFile.internal.getUrl("logo.png", function(url) {
 			alert(url);
 			window.location.href =url;
 		},function(err) {
 			alert("ERROR: getUrl: "+err);
 		});
 	},function(err) {
 		alert("ERROR: write: "+err);
 	});
 },function(err) {
 	alert("ERROR: read: "+err);
 });



