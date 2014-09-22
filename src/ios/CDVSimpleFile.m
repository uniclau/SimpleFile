//
//  CDVSimpleFile.m
//  Uniclau
//
//  Created by Jordi Bylina.
//  Copyright (c) 2014 Uniclau. All rights reserved.
//

#import <Cordova/CDV.h>
#import "CDVSimpleFile.h"
#import "NetRequest.h"

@implementation CDVSimpleFile

-(NSString *)getRootPath: (NSString *)type
{
    NSString *res;
    if ([@"external" isEqualToString:type]) {
        res = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    } else if ([@"internal" isEqualToString:type]) {
        res = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    } else if ([@"bundle" isEqualToString:type]) {
        res = [[NSBundle mainBundle] bundlePath];
    } else if ([@"cache" isEqualToString:type]) {
        res = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    } else if ([@"tmp" isEqualToString:type]) {
        res = [NSTemporaryDirectory()stringByStandardizingPath];
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
        NSData *d = [NSData dataWithContentsOfFile:fullPathFile];
        NSString *d64 = [d base64EncodedStringWithOptions:0];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:d64];
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
        NSData *d = [NSData dataFromBase64String:d64];
        
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
            NSMutableArray *arr = [NSMutableArray init];
            for (NSString *f in contents ) {
                NSMutableDictionary *obj = [NSMutableDictionary init];
                [obj setValue:f forKey:@"name"];
                BOOL isDir;
                [fileManager fileExistsAtPath:f isDirectory:&isDir];
                [obj setValue:[NSNumber numberWithBool:isDir] forKey:@"isDirectory"];
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

@end
