# EEGWorkflow_GUI  

The application consists of two parts   
- Data management - browsing and managing (uploading,deleting...) files/folders on the Hadoop Distributed File System located on a remote Hadoop server  
- Data analysis - building data analysis jobs using GUI elements and tracking them

Note: Please check the wiki section of this repository to see solutions to common issues that might occur

### Data management

The application provides the user with a generic screen capable of: 

- browsing folders using the BACK/NEXT buttons 
- creating a folder using the CREATE FOLDER button
- uploading files using the UPLOAD FILES button
- uploading a folder (recursively) using the UPLOAD FOLDER button
- deleting a file or recursively deleting a folder using the DELETE button
- analyzing data using the ANALYZE button

Demo of the screen:    

<img src="https://user-images.githubusercontent.com/16664769/29004325-edf870f2-7ac5-11e7-845b-341fe69af471.png" width="450" height="450" />
  
  

### Data analysis  

##### Building data analysis jobs 
The Analysis panel provides the user with options of choosing between
- loading an already trained classifier 
- training a new classifier

The Analysis panel can be opened by clicking the Analyze button on either a "info.txt" file or a ".eeg" file.
In case the user wishes to analyze a ".eeg" file, a Guessed number will also have to be provided.

When loading a classifier, the user can choose from a list of implemeted classifier, 
namely: Decision Tree classifier, Random Forest classifier, Support Vector Machine classifier, Logistic Regression classifier.   

##### Tracking the execution of jobs