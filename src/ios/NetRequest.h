//
//  NetRequest.h
//  Uniclau
//
//  Created by Jordi Moraleda on 12/02/12.
//  Copyright (c) 2012 Uniclau. All rights reserved.
//

#import <Foundation/Foundation.h>


#define ERR_CANCELLED   2001
#define ERR_CANCELLED_STR @"Operation Canceled"

// Definitions
#define ALL_REQUESTS_CANCEL_NOTIFICATION @"allRequestsCancel"

typedef void (^NetRequestBlock) (NSData*, NSError*);



// Class
@interface NetRequest : NSObject <NSURLConnectionDelegate>
{
    NetRequestBlock responseBlock;
	NSURLConnection *connection;
	NSMutableData *responseData;
    BOOL terminated;
}

//@property (nonatomic, copy) NetRequestBlock responseBlock;
@property (nonatomic, retain) NSURLConnection *connection;
@property (nonatomic, retain) NSMutableData *responseData;



+(void)post:(NSURLRequest *)request 
 cancelWith:(NSString*)cancelString
   onReturn:(NetRequestBlock)callerBlock;

@end



