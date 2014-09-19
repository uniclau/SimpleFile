SimpleFile
==========

SimpleFile is a phonegap plugin that lets wou read,write files into the app-private local filesysten.

You can also create/remove directories, extract a URI from a file and download files from the web.

API
---

- **readFile: function readFile(fileName, successCallback, errorCallback)**

reads the contents of a file in the first parameter of successCallback
   
- **writeFile: function(fileName, fileData, successCallback, errorCallback)**

wrtes fileData to fileName. If fileName contains directories, they will be created if they does not exist.
   
- **deleteFile: function(fileName, successCallback, errorCallback)**

deletes the file
    
- **getUrlFile: function(fileName, successCallback, errorCallback)**

returns the url of a file
     
- **downloadFile: function(url, fileName, successCallback, errorCallback)**

downloads a file and crete/replace the file in the filesystem.
If fileName contains directories, they will be created if they does not exist.
    
- **createDir: function(dirName, successCallback, errorCallback)**

creates a directory and all the parent directories if the does not exist.
   
- **deleteDir: function(dirName, successCallback, errorCallback)**

deletes a directory and all its contents
   
- **listDir: function(dirName, successCallback, errorCallback)**

returns am array of obects in the first parameter of successCall.

Each object has two values: name (name) and isDirectory (boolean)

If you want to list the root directory just set dirName to "" or to "."


