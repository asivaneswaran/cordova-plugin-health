package org.apache.cordova.health;

import android.util.Log;

import androidx.health.connect.client.aggregate.AggregateMetric;
import androidx.health.connect.client.aggregate.AggregationResult;
import androidx.health.connect.client.records.NutritionRecord;
import androidx.health.connect.client.records.Record;
import androidx.health.connect.client.records.metadata.DataOrigin;
import androidx.health.connect.client.records.metadata.Metadata;
import androidx.health.connect.client.request.AggregateGroupByDurationRequest;
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest;
import androidx.health.connect.client.request.AggregateRequest;
import androidx.health.connect.client.time.TimeRangeFilter;
import androidx.health.connect.client.records.MealType;
import androidx.health.connect.client.units.Energy;
import androidx.health.connect.client.units.Mass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import kotlin.reflect.KClass;

public class NutritionFunctions {
    /**
     * Tag used in logs
    */
    public static String TAG = "cordova-plugin-health";

    public static KClass<? extends Record> dataTypeToClass() {
        return kotlin.jvm.JvmClassMappingKt.getKotlinClass(NutritionRecord.class);
    }

    public static void populateFromQuery(Record datapoint, JSONObject obj) throws JSONException {
        
        JSONObject nutritionStats = new JSONObject();

        NutritionRecord nutritionR = (NutritionRecord) datapoint;
        nutritionStats.put("startDate", nutritionR.getStartTime().toEpochMilli());
        nutritionStats.put("endDate", nutritionR.getEndTime().toEpochMilli());

        String name = nutritionR.getName();
        nutritionStats.put("item", name);

        Double kcal = nutritionR.getEnergy().getKilocalories();
        nutritionStats.put("calories", kcal);

        int mealType = nutritionR.getMealType();
        if(mealType == MealType.MEAL_TYPE_BREAKFAST) {
            nutritionStats.put("meal_type", "breakfast");
        } else if(mealType == MealType.MEAL_TYPE_LUNCH) {
            nutritionStats.put("meal_type", "lunch");
        } else if(mealType == MealType.MEAL_TYPE_DINNER) {
            nutritionStats.put("meal_type", "dinner");
        } else if(mealType == MealType.MEAL_TYPE_SNACK) {
            nutritionStats.put("meal_type", "snack");
        } else {
            nutritionStats.put("meal_type", "unknown");
        }

        Double protein = nutritionR.getProtein().getGrams();
        nutritionStats.put("protein", protein);

        Double fat = nutritionR.getTotalFat().getGrams();
        nutritionStats.put("fat_total", fat);

        Double carbs = nutritionR.getTotalCarbohydrate().getGrams();
        nutritionStats.put("carbs_total", carbs);

        obj.put("value", nutritionStats);
        obj.put("unit", "meal");
    }

    public static void populateFromAggregatedQuery(AggregationResult response, JSONObject retObj) throws JSONException {
        if (response.get(NutritionRecord.ENERGY_TOTAL) != null || response.get(NutritionRecord.PROTEIN_TOTAL) != null || response.get(NutritionRecord.TOTAL_FAT_TOTAL) != null || response.get(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL) != null) {
            JSONObject nutritionStats = new JSONObject();

            Energy totalEnergy = response.get(NutritionRecord.ENERGY_TOTAL);
            nutritionStats.put("calories", totalEnergy != null ? totalEnergy.getKilocalories() : 0);

            Mass totalProtein = response.get(NutritionRecord.PROTEIN_TOTAL);
            nutritionStats.put("protein", totalProtein != null ? totalProtein.getGrams() : 0);

            Mass totalFat = response.get(NutritionRecord.TOTAL_FAT_TOTAL);
            nutritionStats.put("fat_total", totalFat != null ? totalFat.getGrams() : 0);

            Mass totalCarbs = response.get(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL);
            nutritionStats.put("carbs_total", totalCarbs != null ? totalCarbs.getGrams() : 0);

            Log.d(TAG, "response: " + response);
            Log.d(TAG, "nutrition stats: " + nutritionStats);

            Log.d(TAG, "carbs: " + totalCarbs);

            retObj.put("value", nutritionStats);
            retObj.put("unit", "meal");
        } else {
            JSONObject emptyObject = new JSONObject();
            retObj.put("value", emptyObject);
            retObj.put("unit", "meal");
        }
    }

    public static AggregateGroupByPeriodRequest prepareAggregateGroupByPeriodRequest (TimeRangeFilter timeRange, Period period, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Energy>> metrics = new HashSet<>();
        metrics.add(NutritionRecord.ENERGY_TOTAL);

        return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
    }

    public static AggregateGroupByDurationRequest prepareAggregateGroupByDurationRequest (TimeRangeFilter timeRange, Duration duration, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Energy>> metrics = new HashSet<>();
        metrics.add(NutritionRecord.ENERGY_TOTAL);
        return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
    }

    public static AggregateRequest prepareAggregateRequest(TimeRangeFilter timeRange, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Energy>> metrics = new HashSet<>();
        metrics.add(NutritionRecord.ENERGY_TOTAL);
        return new AggregateRequest(metrics, timeRange, dor);
    }

    public static AggregateGroupByPeriodRequest prepareAggregateGroupByPeriodRequestMacros (TimeRangeFilter timeRange, Period period, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Mass>> metrics = new HashSet<>();
        metrics.add(NutritionRecord.PROTEIN_TOTAL);
        metrics.add(NutritionRecord.TOTAL_FAT_TOTAL);
        metrics.add(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL);

        return new AggregateGroupByPeriodRequest(metrics, timeRange, period, dor);
    }

    public static AggregateGroupByDurationRequest prepareAggregateGroupByDurationRequestMacros (TimeRangeFilter timeRange, Duration duration, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Mass>> metrics = new HashSet<>();
         metrics.add(NutritionRecord.PROTEIN_TOTAL);
        metrics.add(NutritionRecord.TOTAL_FAT_TOTAL);
        metrics.add(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL);
        return new AggregateGroupByDurationRequest(metrics, timeRange, duration, dor);
    }

    public static AggregateRequest prepareAggregateRequestMacros (TimeRangeFilter timeRange, HashSet<DataOrigin> dor) {
        Set<AggregateMetric<Mass>> metrics = new HashSet<>();
        metrics.add(NutritionRecord.PROTEIN_TOTAL);
        metrics.add(NutritionRecord.TOTAL_FAT_TOTAL);
        metrics.add(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL);
        return new AggregateRequest(metrics, timeRange, dor);
    }

    public static void prepareStoreRecords(JSONObject storeObj, long st, long et, List<Record> data) throws JSONException {
        int mealType = MealType.MEAL_TYPE_UNKNOWN;
      
        if (storeObj.has("meal")) {
            String meal = storeObj.getString("meal_type");
  
            if (meal.equalsIgnoreCase("dinner")) {
                mealType = MealType.MEAL_TYPE_DINNER;
            } else if (meal.equalsIgnoreCase("lunch")) {
                mealType = MealType.MEAL_TYPE_LUNCH;
            } else if (meal.equalsIgnoreCase("snack")) {
                mealType = MealType.MEAL_TYPE_SNACK;
            } else if (meal.equalsIgnoreCase("breakfast")) {
                mealType = MealType.MEAL_TYPE_BREAKFAST;
            }
        }

        Log.d(TAG, "mealType: " + mealType);
        Log.d(TAG, "storeObj: " + storeObj);

        Log.d(TAG, "storeObj: " + storeObj.getDouble("carbs_total"));

        double kcal = storeObj.getDouble("calories");
        double protein = storeObj.getDouble("protein");
        double fat = storeObj.getDouble("fat_total");
        double carbs = storeObj.getDouble("carbs_total");
        String name = storeObj.getString("item");

        NutritionRecord record = new NutritionRecord(
                Instant.ofEpochMilli(st),
                null,
                Instant.ofEpochMilli(et),
                null,
                null,
                null,
                null,
                Energy.kilocalories(kcal), // Energy
                null, // Energy from fat
                null, // Chloride
                null, // Cholesterol
                null, // Chromium
                null, // Copper
                null, // Dietary fiber
                null, // Folate
                null, // Folid acid
                null, // Iodine
                null, // Iron
                null, // Magnesium
                null, // Manganese
                null, // Molybdenum
                null, // Monosaturated fat
                null, // Niacin
                null, // Pantothenic acid
                null, // Phosphorus
                null, // Polyunsaturated fat
                null, // Potassium
                Mass.grams(protein), // Protein
                null, // Riboflavin
                null, // Saturated fat
                null, // Selenium
                null, // Sodium
                null, // Sugars
                null, // Thiamin
                Mass.grams(carbs), // Total carbohydrate
                Mass.grams(fat), // Total fat
                null, // Trans fat
                null, // Unsaturated fat
                null, // Vitamin A
                null, // Vitamin B12
                null, // Vitamin B6
                null, // Vitamin C
                null, // Vitamin D
                null, // Vitamin E
                null, // Vitamin K
                null, // Zinc
                name,
                mealType,
                Metadata.EMPTY
        );
        data.add(record);
    }

}
