//
//  CDVSimpleFile.m
//  Uniclau
//
//  Created by Jordi Bylina.
//  Copyright (c) 2014 Uniclau. All rights reserved.
//

#import <Cordova/CDV.h>
#include <sys/xattr.h>
#import "CDVSimpleFile.h"
#import "NetRequest.h"

@implementation CDVSimpleFile


//// BASE64 encoding

static const char _base64EncodingTable[64] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

static const short _base64DecodingTable[256] = {
    -2, -2, -2, -2, -2, -2, -2, -2, -2, -1, -1, -2, -1, -1, -2, -2,
    -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,
    -1, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, 62, -2, -2, -2, 63,
    52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -2, -2, -2, -2, -2, -2,
    -2,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
    15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -2, -2, -2, -2, -2,
    -2, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
    41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -2, -2, -2, -2, -2,
    -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,
    -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,
    -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,
    -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,
    -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,
    -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,
    -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,
    -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2
};

+ (NSString *)encodeBase64WithString:(NSString *)strData {
    return [CDVSimpleFile encodeBase64WithData:[strData dataUsingEncoding:NSUTF8StringEncoding]];
}

//http://www.accommodationhepburnsprings.com/hepburn-springs-accommodation/
+ (NSString *)encodeBase64WithData:(NSData *)objData
{
    const unsigned char * objRawData = [objData bytes];
    char * objPointer;
    char * strResult;
    
    // Get the Raw Data length and ensure we actually have data
    int intLength = [objData length];
    if (intLength == 0)
        return @"";
    
    // Setup the String-based Result placeholder and pointer within that placeholder
    strResult = (char *)calloc(((intLength + 2) / 3) * 4+1, sizeof(char));
    objPointer = strResult;
    
    // Iterate through everything
    while (intLength > 2) // keep going until we have less than 24 bits
    {
        *objPointer++ = _base64EncodingTable[objRawData[0] >> 2];
        *objPointer++ = _base64EncodingTable[((objRawData[0] & 0x03) << 4) + (objRawData[1] >> 4)];
        *objPointer++ = _base64EncodingTable[((objRawData[1] & 0x0f) << 2) + (objRawData[2] >> 6)];
        *objPointer++ = _base64EncodingTable[objRawData[2] & 0x3f];
        
        // we just handled 3 octets (24 bits) of data
        objRawData += 3;
        intLength -= 3;
    }
    
    // now deal with the tail end of things
    if (intLength != 0)
    {
        *objPointer++ = _base64EncodingTable[objRawData[0] >> 2];
        if (intLength > 1)
        {
            *objPointer++ = _base64EncodingTable[((objRawData[0] & 0x03) << 4) + (objRawData[1] >> 4)];
            *objPointer++ = _base64EncodingTable[(objRawData[1] & 0x0f) << 2];
            *objPointer++ = '=';
        }
        else
        {
            *objPointer++ = _base64EncodingTable[(objRawData[0] & 0x03) << 4];
            *objPointer++ = '=';
            *objPointer++ = '=';
        }
    }
    
    // Terminate the string-based result
    *objPointer = '\0';
    
    NSString *Res=[NSString stringWithCString:strResult encoding:NSASCIIStringEncoding];
    
    free(strResult);
    
    // Return the results as an NSString object
    return Res;
}

+ (NSData *)decodeBase64WithString:(NSString *)strBase64
{
    if (![[strBase64 class] isSubclassOfClass:[NSString class]]) return nil;
    const char * objPointer = [strBase64 cStringUsingEncoding:NSUTF8StringEncoding];
    if (objPointer==nil) return nil;
    int intLength = strlen(objPointer);
    int intCurrent;
    int i = 0, j = 0, k;
    
    unsigned char * objResult;
    objResult = calloc(intLength, sizeof(char));
    
    // Run through the whole string, converting as we go
    while ( ((intCurrent = *objPointer++) != '\0') && (intLength-- > 0) )
    {
        if (intCurrent == '=')
        {
            if (*objPointer != '=' && ((i % 4) == 1)) // || (intLength > 0)) {
            {
                // the padding character is invalid at this point -- so this entire string is invalid
                free(objResult);
                return nil;
            }
            continue;
        }
        
        intCurrent = _base64DecodingTable[intCurrent];
        if (intCurrent == -1)
        {
            // we're at a whitespace -- simply skip over
            continue;
        }
        else if (intCurrent == -2)
        {
            // we're at an invalid character
            free(objResult);
            return nil;
        }
        
        switch (i % 4)
        {
            case 0:
                objResult[j] = intCurrent << 2;
                break;
                
            case 1:
                objResult[j++] |= intCurrent >> 4;
                objResult[j] = (intCurrent & 0x0f) << 4;
                break;
                
            case 2:
                objResult[j++] |= intCurrent >>2;
                objResult[j] = (intCurrent & 0x03) << 6;
                break;
                
            case 3:
                objResult[j++] |= intCurrent;
                break;
        }
        i++;
    }
    
    // mop things up if we ended on a boundary
    k = j;
    if (intCurrent == '=')
    {
        switch (i % 4)
        {
            case 1:
                // Invalid state
                free(objResult);
                return nil;
                
            case 2:
                k++;
                // flow through
            case 3:
                objResult[k] = 0;
        }
    }
    
    // Cleanup and setup the return NSData
    NSData * objData = [[NSData alloc] initWithBytes:objResult length:j] ;
    free(objResult);
    return objData;
}

- (void)addSkipBackupAttributeToPath:(NSString*)path {
    u_int8_t b = 1;
    setxattr([path fileSystemRepresentation], "com.apple.MobileBackup", &b, 1, 0, 0);
}

- (void)pluginInitialize
{
    NSString *noCloudPath=[self getRootPath:@"internal"];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error;
    [fileManager createDirectoryAtPath:noCloudPath withIntermediateDirectories:YES attributes:nil error:&error];
    [self addSkipBackupAttributeToPath: noCloudPath];
}

-(NSString *)getRootPath: (NSString *)type
{
    NSString *res;
    if ([@"external" isEqualToString:type]) {
        res = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    } else if ([@"internal" isEqualToString:type]) {
        res = [[NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) objectAtIndex:0] stringByAppendingPathComponent: @"NoCloud"];
    } else if ([@"user" isEqualToString:type]) {
        res = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    } else if ([@"bundle" isEqualToString:type]) {
        res = [[NSBundle mainBundle] bundlePath];
    } else if ([@"cache" isEqualToString:type]) {
        res = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    } else if ([@"tmp" isEqualToString:type]) {
        res = [NSTemporaryDirectory() stringByStandardizingPath];
    }
    
    return res;
}


- (void)read:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    @try {
        NSString *root=[self getRootPath:[command.arguments objectAtIndex:0]];
        NSString *fileName = [command.arguments objectAtIndex:1];
        NSString *fullPathFile = [root stringByAppendingPathComponent:fileName];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        
        if (![fileManager fileExistsAtPath:fullPathFile]) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"File does not exists"];
        } else {
            NSData *d = [NSData dataWithContentsOfFile:fullPathFile];
            NSString *d64 = [CDVSimpleFile encodeBase64WithData:d];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:d64];
        }
    } @catch (NSException *e) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[e reason]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)write:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    @try {
        NSString *root=[self getRootPath:[command.arguments objectAtIndex:0]];
        NSString *fileName = [command.arguments objectAtIndex:1];
        NSString *fullPathFile = [root stringByAppendingPathComponent:fileName];
        NSString *d64 = [command.arguments objectAtIndex:2];
        NSData *d = [CDVSimpleFile decodeBase64WithString:d64];
        
        NSString *parentDir = [fullPathFile stringByDeletingLastPathComponent];
        
        NSError *error;
        NSFileManager *fileManager = [NSFileManager defaultManager];
        BOOL success = [fileManager createDirectoryAtPath:parentDir withIntermediateDirectories:YES attributes:nil error:&error];
        if (success) {
            [d writeToFile:fullPathFile atomically:true];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error description]];
        }
    } @catch (NSException *e) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[e reason]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)remove:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    @try {
        NSString *root=[self getRootPath:[command.arguments objectAtIndex:0]];
        NSString *fileName = [command.arguments objectAtIndex:1];
        NSString *fullPathFile = [root stringByAppendingPathComponent:fileName];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        
        NSError *error;
        BOOL success = [fileManager removeItemAtPath:fullPathFile error:&error];
        if (success) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error description]];
        }
    } @catch (NSException *e) {
        [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[e reason]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getUrl:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    @try {
        NSString *root=[self getRootPath:[command.arguments objectAtIndex:0]];
        NSString *fileName = [command.arguments objectAtIndex:1];
        NSString *fullPathFile = [root stringByAppendingPathComponent:fileName];

        NSString *res = [NSString stringWithFormat:@"file://%@",fullPathFile];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:res];
    } @catch (NSException *e) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[e reason]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)download:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    @try {
        NSString *root=[self getRootPath:[command.arguments objectAtIndex:0]];
        NSString *url= [command.arguments objectAtIndex:1];
        NSString *fileName = [command.arguments objectAtIndex:2];
        NSString *fullPathFile = [root stringByAppendingPathComponent:fileName];
        
        NSMutableURLRequest * urlRequest = [[NSMutableURLRequest alloc] initWithURL:[NSURL URLWithString:url]];

        [NetRequest post:urlRequest
              cancelWith:@"cancel"
                onReturn:^(NSData *data, NSError *err)
         {
             CDVPluginResult* pluginResult = nil;
             @try {
                 if(err != nil) {
                     pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[err description]];
                 } else {
                     NSString *parentDir = [fullPathFile stringByDeletingLastPathComponent];
   
                     NSError *error;
                     NSFileManager *fileManager = [NSFileManager defaultManager];
                     BOOL success = [fileManager createDirectoryAtPath:parentDir withIntermediateDirectories:YES attributes:nil error:&error];
                     if (success) {
                         [data writeToFile:fullPathFile atomically:true];
                         pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
                     } else {
                         pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error description]];
                     }
                 }
             } @catch (NSException *e) {
                 pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[e reason]];
             }
             [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
         }];
    } @catch (NSException *e) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[e reason]];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void)createFolder:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    @try {
        NSString *root=[self getRootPath:[command.arguments objectAtIndex:0]];
        NSString *fileName = [command.arguments objectAtIndex:1];
        NSString *fullPathFile = [root stringByAppendingPathComponent:fileName];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        
        NSError *error;
        BOOL success = [fileManager createDirectoryAtPath:fullPathFile withIntermediateDirectories:YES attributes:nil error:&error];
        if (success) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error description]];
        }
    } @catch (NSException *e) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[e reason]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)list:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    @try {
        NSString *root=[self getRootPath:[command.arguments objectAtIndex:0]];
        NSString *fileName = [command.arguments objectAtIndex:1];
        NSString *fullPathFile = [root stringByAppendingPathComponent:fileName];
        NSFileManager *fileManager = [NSFileManager defaultManager];
        
        NSError *error;
        NSArray *contents = [fileManager contentsOfDirectoryAtPath:fullPathFile error:&error];
        if (contents != nil) {
            NSMutableArray *arr = [[NSMutableArray alloc] init];
            for (NSString *f in contents ) {
                NSMutableDictionary *obj = [[NSMutableDictionary alloc] init];
                [obj setValue:f forKey:@"name"];
                NSString *fullF = [fullPathFile stringByAppendingPathComponent:f];
                BOOL isDir;
                [fileManager fileExistsAtPath:fullF isDirectory:&isDir];
                [obj setValue:[NSNumber numberWithBool:isDir] forKey:@"isFolder"];
                [arr addObject:obj];
            }
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:arr];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error description]];
        }
    } @catch (NSException *e) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[e reason]];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)copyFrom:(NSString *)from to:(NSString *)to
{
    BOOL success;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL isDir;
    BOOL exist;
    exist = [fileManager fileExistsAtPath:from isDirectory:&isDir];
    if (!exist) {
        [NSException raise:@"File does not exist" format:@" %@", from];
    }
    if (isDir) {
        NSString *newDir =  [to stringByAppendingPathComponent:[from lastPathComponent] ];
        NSError *error;
        success = [fileManager createDirectoryAtPath:newDir withIntermediateDirectories:YES attributes:nil error:&error];
        if (!success) {
            [NSException raise:@"Error creating directory" format:@"%@: %@", [error description], newDir];
        }
        NSArray *contents = [fileManager contentsOfDirectoryAtPath:from error:&error];
        if (contents != nil) {
            for (NSString *f in contents ) {
                NSString *newFrom = [from stringByAppendingPathComponent:f];
                NSString *newTo = [to stringByAppendingPathComponent:f];
                [self copyFrom: newFrom to:newTo];
            }
        } else {
            [NSException raise:@"It is not a directory" format:@" %@", from];
        }
    } else {
        NSData *data = [NSData dataWithContentsOfFile:from];
        if (data==nil) {
            [NSException raise:@"Error reading file" format:@" %@", from];
        }
        success = [data writeToFile:to atomically:true];
        if (!success) {
            [NSException raise:@"Error wrting file" format:@" %@", to];
        }
    }
}

- (void)copy:(CDVInvokedUrlCommand*)command
{
    
    [self.commandDelegate runInBackground:^{
        
        CDVPluginResult* pluginResult = nil;
        @try {
            NSString *rootFrom=[self getRootPath:[command.arguments objectAtIndex:0]];
            NSString *fileFrom = [command.arguments objectAtIndex:1];
            NSString *rootTo=[self getRootPath:[command.arguments objectAtIndex:2]];
            NSString *fileTo = [command.arguments objectAtIndex:3];
            NSString *fullPathFileFrom = [rootFrom stringByAppendingPathComponent:fileFrom];
            NSString *fullPathFileTo = [rootTo stringByAppendingPathComponent:fileTo];
            [self copyFrom:fullPathFileFrom to:fullPathFileTo ];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } @catch (NSException *e) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[e reason]];
        }
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}



@end
