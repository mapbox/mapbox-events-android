package com.mapbox.services.android.telemetry;

import java.util.HashMap;
import java.util.Map;

public class NavigationEventFactory {

  private final Map<Event.Type, NavBuildEvent> BUILD_NAV_EVENT = new HashMap<Event.Type, NavBuildEvent>() {
    {
      put(Event.Type.NAV_ARRIVE, new NavBuildEvent() {
        @Override
        public Event build(NavigationState navigationState) {
          return buildNavigationArriveEvent(navigationState);
        }
      });
      put(Event.Type.NAV_DEPART, new NavBuildEvent() {
        @Override
        public Event build(NavigationState navigationState) {
          return buildNavigationDepartEvent(navigationState);
        }
      });
      put(Event.Type.NAV_CANCEL, new NavBuildEvent() {
        @Override
        public Event build(NavigationState navigationState) {
          return buildNavigationCancelEvent(navigationState);
        }
      });
      put(Event.Type.NAV_FEEDBACK, new NavBuildEvent() {
        @Override
        public Event build(NavigationState navigationState) {
          return buildNavigationFeedbackEvent(navigationState);
        }
      });
      put(Event.Type.NAV_REROUTE, new NavBuildEvent() {
        @Override
        public Event build(NavigationState navigationState) {
          return buildNavigationRerouteEvent(navigationState);
        }
      });
      put(Event.Type.NAV_FASTER, new NavBuildEvent() {
        @Override
        public Event build(NavigationState navigationState) {
          return buildNavigationFasterRouteEvent(navigationState);
        }
      });
    }
  };

  public Event createNavigationEvent(Event.Type type, NavigationState navigationState) {
    return BUILD_NAV_EVENT.get(type).build(navigationState);
  }

  private NavigationDepartEvent buildNavigationDepartEvent(NavigationState navigationState) {
    return new NavigationDepartEvent(navigationState);
  }

  private NavigationArriveEvent buildNavigationArriveEvent(NavigationState navigationState) {
    return new NavigationArriveEvent(navigationState);
  }

  private NavigationCancelEvent buildNavigationCancelEvent(NavigationState navigationState) {
    return new NavigationCancelEvent(navigationState);
  }

  private NavigationRerouteEvent buildNavigationRerouteEvent(NavigationState navigationState) {
    return new NavigationRerouteEvent(navigationState);
  }

  private NavigationFeedbackEvent buildNavigationFeedbackEvent(NavigationState navigationState) {
    return  new NavigationFeedbackEvent(navigationState);
  }

  private NavigationFasterRouteEvent buildNavigationFasterRouteEvent(NavigationState navigationState) {
    return  new NavigationFasterRouteEvent(navigationState);
  }
}
