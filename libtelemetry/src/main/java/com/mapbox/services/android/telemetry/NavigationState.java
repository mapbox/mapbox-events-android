package com.mapbox.services.android.telemetry;

class NavigationState {
  private NavigationMetadata navigationMetadata;
  private NavigationStepMetadata navigationStepMetadata;
  private NavigationCancelData navigationCancelData;
  private NavigationLocationData navigationLocationData;
  private NavigationRerouteData navigationRerouteData;
  private FeedbackEventData feedbackEventData;
  private NavigationVoiceData navigationVoiceData;
  private FeedbackData feedbackData;

  NavigationState(NavigationMetadata navigationMetadata) {
    this.navigationMetadata = navigationMetadata;
  }

  NavigationMetadata getNavigationMetadata() {
    return navigationMetadata;
  }

  NavigationStepMetadata getNavigationStepMetadata() {
    return navigationStepMetadata;
  }

  void setNavigationStepMetadata(NavigationStepMetadata navigationStepMetadata) {
    this.navigationStepMetadata = navigationStepMetadata;
  }

  NavigationCancelData getNavigationCancelData() {
    return navigationCancelData;
  }

  void setNavigationCancelData(NavigationCancelData navigationCancelData) {
    this.navigationCancelData = navigationCancelData;
  }

  NavigationLocationData getNavigationLocationData() {
    return navigationLocationData;
  }

  void setNavigationLocationData(NavigationLocationData navigationLocationData) {
    this.navigationLocationData = navigationLocationData;
  }

  NavigationRerouteData getNavigationRerouteData() {
    return navigationRerouteData;
  }

  void setNavigationRerouteData(NavigationRerouteData navigationRerouteData) {
    this.navigationRerouteData = navigationRerouteData;
  }

  FeedbackEventData getFeedbackEventData() {
    return feedbackEventData;
  }

  void setFeedbackEventData(FeedbackEventData feedbackEventData) {
    this.feedbackEventData = feedbackEventData;
  }

  NavigationVoiceData getNavigationVoiceData() {
    return navigationVoiceData;
  }

  void setNavigationVoiceData(NavigationVoiceData navigationVoiceData) {
    this.navigationVoiceData = navigationVoiceData;
  }

  FeedbackData getFeedbackData() {
    return feedbackData;
  }

  void setFeedbackData(FeedbackData feedbackData) {
    this.feedbackData = feedbackData;
  }
}