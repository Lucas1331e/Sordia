$jar = "C:\Users\pinhe\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-merged\1.21.11-net.fabricmc.yarn.1_21_11.1.21.11+build.4-v2\minecraft-merged-1.21.11-net.fabricmc.yarn.1_21_11.1.21.11+build.4-v2.jar"

Write-Host "--- SingleStackRecipe ---"
javap -cp $jar -p net.minecraft.recipe.SingleStackRecipe

Write-Host "--- RawShapedRecipeData ---"
javap -cp $jar -p net.minecraft.recipe.RawShapedRecipe\$Data

