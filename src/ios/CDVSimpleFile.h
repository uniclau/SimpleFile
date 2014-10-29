//
//  CDVSimpleFile.m
//  Uniclau
//
//  Created by Jordi Bylina.
//  Copyright (c) 2014 Uniclau. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>


@interface CDVSimpleFile : CDVPlugin {
}


/* Exec API */
- (void)read:(CDVInvokedUrlCommand*)command;
- (void)write:(CDVInvokedUrlCommand*)command;
- (void)remove:(CDVInvokedUrlCommand*)command;
- (void)getUrl:(CDVInvokedUrlCommand*)command;
- (void)download:(CDVInvokedUrlCommand*)command;
- (void)createFolder:(CDVInvokedUrlCommand*)command;
- (void)list:(CDVInvokedUrlCommand*)command;
- (void)copy:(CDVInvokedUrlCommand*)command;
- (void)pluginInitialize;

@end

