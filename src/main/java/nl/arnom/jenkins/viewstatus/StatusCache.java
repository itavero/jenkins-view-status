package nl.arnom.jenkins.viewstatus;

import hudson.init.Initializer;
import hudson.model.*;
import jenkins.model.Jenkins;
import nl.arnom.jenkins.viewstatus.notifier.ViewStatusNotifiersProperty;

import java.util.*;
import java.util.logging.Logger;

import static hudson.init.InitMilestone.COMPLETED;
import static hudson.init.InitMilestone.JOB_LOADED;

public class StatusCache {

  private final static Logger LOGGER = Logger.getLogger(StatusCache.class.getName());
  private final static StatusCache CACHE = new StatusCache();
  private final Map<View, Result> viewResults = new HashMap<>();

  @Initializer(after = JOB_LOADED, before = COMPLETED)
  public static void setupViewStatusCache() {
    LOGGER.info("Setting up " + StatusCache.class.getSimpleName());

    Jenkins jenkins = Jenkins.getInstanceOrNull();
    if (jenkins != null) {
      CACHE.loadViews(jenkins.getViews());
    } else {
      LOGGER.severe("Cannot get instance of Jenkins");
    }
  }

  private void callListeners(View view, Result previous) {
    callListeners(view, previous, false);
  }

  private void callListeners(View view, Result previous, boolean fromStartup) {
    if (view == null) {
      return;
    }
    synchronized (viewResults) {
      if (!viewResults.containsKey(view)) {
        return;
      }

      Result current = getViewResult(view);

      if (previous == null || !previous.equals(current)) {
        String previousAsString = "NONE";
        if (previous != null) {
          previousAsString = previous.toString();
        }
        LOGGER.info("Status of view '" + view.getDisplayName() +"' changed from " + previousAsString + " to " + current.toString());
        view.getAllProperties().stream()
              .filter(property -> property instanceof ViewStatusNotifiersProperty)
              .flatMap(property -> Arrays.stream(((ViewStatusNotifiersProperty) property).getNotifiers()))
              .map(notifier -> new Thread(() -> notifier.doNotify(view, current, previous, fromStartup)))
              .parallel()
              .forEach(t -> t.start());
      }
    }
  }

  private void loadViews(Collection<View> views) {
    for (View view : views) {
      recalculateStatusOfView(view);
      callListeners(view, null, true);
    }
  }

  private void recalculateStatusOfView(View view) {
    Result viewResult = null;
    // TODO Need to handle Folders here (not handled correctly now).
    for (TopLevelItem item : view.getAllItems()) {
      LOGGER.finest("Checking TopLevelItem " + item.getFullDisplayName());
      for (Job job : item.getAllJobs()) {
        try {
          LOGGER.finest("Checking Job " + job.getDisplayName());
          Result lastResult = job.getLastCompletedBuild().getResult();
          if (lastResult.completeBuild) {
            if (viewResult == null) {
              viewResult = lastResult;
            } else {
              viewResult = lastResult.combine(lastResult);
            }
          }
        } catch (NullPointerException ex) {
          // ignore
        }
      }
    }

    if (viewResult != null) {
      synchronized(viewResults) {
        viewResults.put(view, viewResult);
      }
    }
  }

  public Map<View, Result> getCopyOfResults() {
    Map<View, Result> copy;
    synchronized (viewResults) {
      copy = new HashMap<>(viewResults);
    }
    return Collections.unmodifiableMap(copy);
  }

  public Result getViewResult(View view) {
    synchronized(viewResults) {
      return viewResults.getOrDefault(view, Result.NOT_BUILT);
    }
  }

  public Result getViewResult(String viewName) {
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j != null) {
      View v = j.getView(viewName);
      if (v != null) {
        return getViewResult(v);
      }
    }
    return Result.NOT_BUILT;
  }

  public void appendResultForView(View view, Result result) {
    if (view == null || result == null) {
      return;
    }
    if (!result.completeBuild) {
      LOGGER.warning("combineResultForView called with uncompleted result: " + result.toString());
      return;
    }
    LOGGER.info("Appending result " + result.toString() + " to result of view " + view.getDisplayName());

    Result resultAtStart;
    synchronized(viewResults) {
      resultAtStart = viewResults.get(view);
      if (resultAtStart == null || resultAtStart.isWorseThan(result)) {
        recalculateStatusOfView(view);
      } else if (result.isWorseThan(resultAtStart)){
        viewResults.put(view, result);
      }
    }
    callListeners(view, resultAtStart);
  }

  public void forceRecalculateStatusOfView(View view) {
    if (view == null) {
      return;
    }

    LOGGER.info("Force recalculate status of view " + view.getDisplayName());

    Result resultAtStart = getViewResult(view);
    recalculateStatusOfView(view);
    callListeners(view, resultAtStart);
  }

  public static StatusCache getInstance() {
    return CACHE;
  }
}
