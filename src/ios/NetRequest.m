//
//  NetRequest.m
//  Uniclau
//
//  Created by Jordi Bylina.
//  Copyright (c) 2014 Uniclau. All rights reserved.
//

#import "NetRequest.h"

@implementation NetRequest

//@synthesize responseBlock;
@synthesize connection;
@synthesize responseData;

#pragma mark Instance

// Subscribe to the cancel message and set the result code block
-(NetRequest*)init:(NSString*)cancelNotif withResponse:(NetRequestBlock)callerBlock
{
    self = [super init];
    
    // Subscribe to cancel myself
    [[NSNotificationCenter defaultCenter]
         addObserver:self
         selector:@selector(cancel:)
         name:cancelNotif
         object:nil];
    
    // Subscribe to cancel all of ourselves
    [[NSNotificationCenter defaultCenter]
         addObserver:self
         selector:@selector(cancel:)
         name:ALL_REQUESTS_CANCEL_NOTIFICATION
         object:nil];
    
    responseData = [[NSMutableData alloc] init];
    
    responseBlock = [callerBlock copy];
    //connection = nil;
    
    terminated  = false;
    
    return self;
}

// Ready to execute the response code
- (void)terminate:(NSError *)error
{
	if ((responseBlock != nil)&&(!terminated))
    {
        [[NSNotificationCenter defaultCenter] removeObserver:self];
        terminated=true;
        responseBlock(responseData, error);
    }
}

// Cancel our current connection
-(void)cancel:(NSNotification *)notif
{
    
    NSString *CancelData=[NSString stringWithFormat:@"{\"Result\":%d, \"ResultStr\":\"%@\"}", ERR_CANCELLED, ERR_CANCELLED_STR];
    [responseData setData: [CancelData dataUsingEncoding:NSUTF8StringEncoding]];
    [self terminate:nil];
    if(connection != nil)
    {
        [connection cancel];
    }
        
}



#pragma mark -
#pragma mark Global

// Make an Asynchronous Request and execute the given code block
+(void)post:(NSURLRequest *)request 
 cancelWith:(NSString*)cancelString
   onReturn:(NetRequestBlock)callerBlock
{
    NetRequest * newReq = [[NetRequest alloc] init:cancelString withResponse:callerBlock];
    
    NSURLConnection * conn = [[NSURLConnection alloc] initWithRequest:request delegate:newReq];	
    [newReq setConnection:conn];
}

#pragma mark -
#pragma mark NSURLConnectionDelegate

- (void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response
{
	[responseData setLength:0];
}

- (void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data
{
	[responseData appendData:data];
}

- (void)connection:(NSURLConnection *)urlConnection didFailWithError:(NSError *)error
{
	[responseData setLength:0];
    
	[self terminate:error];
    
}

- (void)connectionDidFinishLoading:(NSURLConnection *)urlConnection
{
	[self terminate:nil];
    
}

@end
