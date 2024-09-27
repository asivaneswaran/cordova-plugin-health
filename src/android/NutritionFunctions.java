package org.apache.cordova.health;

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
        nutritionStats.put("fat.total", fat);

        Double carbs = nutritionR.getTotalCarbohydrate().getGrams();
        nutritionStats.put("carbs.total", carbs);

        nutritionStats.put("value", nutritionStats);
        nutritionStats.put("unit", "meal");
    }

    public static void populateFromAggregatedQuery(AggregationResult response, JSONObject retObj) throws JSONException {
        if (response.get(NutritionRecord.ENERGY_TOTAL) != null) {
            JSONObject nutritionStats = new JSONObject();

            double totalEnergy = response.get(NutritionRecord.ENERGY_TOTAL);
            nutritionStats.put("calories", totalEnergy ? totalEnergy.getKilocalories() : 0);

            double totalProtein = response.get(NutritionRecord.PROTEIN_TOTAL);
            nutritionStats.put("protein", totalProtein ? totalProtein.getGrams() : 0);

            double totalFat = response.get(NutritionRecord.TOTAL_FAT_TOTAL);
            nutritionStats.put("fat.total", totalFat ? totalFat.getGrams() : 0);

            double totalCarbs = response.get(NutritionRecord.TOTAL_CARBOHYDRATE_TOTAL);
            nutritionStats.put("carbs.total", totalCarbs ? totalCarbs.getGrams() : 0);

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

        double kcal = storeObj.getDouble("calories");
        double protein = storeObj.getDouble("protein");
        double fat = storeObj.getDouble("fat.total");
        double carbs = storeObj.getDouble("carbs.total");
        String name = storeObj.getString("item");

        NutritionRecord record = new NutritionRecord(
                Instant.ofEpochMilli(st),
                null,
                Instant.ofEpochMilli(st),
                null,
                null,
                null,
                null,
                Energy.kilocalories(kcal),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Mass.grams(protein),
                null,
                null,
                null,
                null,
                null,
                null,
                Mass.grams(carbs),
                Mass.grams(fat),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                name,
                mealType,
                Metadata.EMPTY
        );
        data.add(record);
    }

}
