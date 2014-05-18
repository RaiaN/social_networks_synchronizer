package com.example.SocialNetworksSynchronizer;

import java.util.ArrayList;

public interface AsyncTaskListener {

    void onTaskBegin(String caption, int maxValue, int []buttonIndexes);
    void onTaskProgress(int progress);
    void onTaskCompleted(String caption, ArrayList<Contact> friends, int []buttonIndexes);

    void onTaskBegin(String caption, int []buttonIndexes);
    void onTaskCompleted(String caption, int []buttonIndexes);
}
