Name: TJ Stein
Date: 12/09/2014
Assignment: Web Crawler

-------
 USAGE
-------

- COMPILATION -

I have provided a Makefile, which can be used for compilation and cleaning.
Just run:
 $ make
to compile, and
 $ make clean
to clean.

- RUNNING -

To run the program, the syntax is:

 $ java WebCrawler <mode> <limit> -url <urls...> -search <terms...> [-v]

<mode> = mode the crawler is running in, options are 'bytes' or 'pages' for bytes/pages per second.
<limit> = <mode> per second limit.
-url denotes the start of the starting URLs.
<urls...> = 1 or more starting URLs.
-search denotes the start of the search terms.
<terms...> = 1 or more search terms.
-v = verbose mode. Prints much more information in this mode.

All command line arugments are required except for verbose mode.

While the program is running, if you want to close it and view the found pages, simply type
 $ quit
This will end the web crawler and print the found websites.

All source code for sites visited is stored in the /tmp/ folder.

--------
 DESIGN
--------

This project was separated into 2 files: a Driver and a Worker Thread.

 - Driver -

The driver for my project is the WebCrawler class. This class parses the command line arguments, passes them to
the Worker Thread, and then listens for the user's input to end the crawler.

- Worker -

The worker thread for my project is where all the actual work happens. The class is CrawlerThread, and it is running
on a separate thread so that the Driver can listen for user input. The class contains a subclass called SiteScore,
which is just a Tuple class containing a String and an int, used to help sort the scored sites after the program finishes.

The main things that the thread keeps track of are:
- A list of visited sites
- A queue of sites to visit
- A priority queue of scored sites
along with the command line arugments passed from the driver.

The process of the worker is as so:
- Get the site URL from the queue.
- Add the URL to list of visited sites.
- Check the domain for a robots.txt. Skip the site if robots.txt disallows
- Get the source text for the site.
	- This calls a helper function based on the mode. The helper function keeps track of the limit and sleeps if
	- it has been reached within the time frame.
- Count all instances of the search terms in the source text. This is the score of the site.
- Parse out all URLs in the source text. If any have already been visited, ignore them.
- Store the source text in a new file in the /tmp/ directory.
- Store the url and score in the priority queue.

All of this is occuring in a while loop, who's condition is based on the queue not being empty, and a 'running' variable,
that is set on creation, and is set to false by the driver when it quits.

Once the crawler end, be it from emptying the queue or being terminated by the Driver, it proceeds to print out, highest 
to lowest, all the sites that have a score higher than 0.
