output_dir := app/build/outputs/apk/release
unsigned_apk_filename := app-release-unsigned.apk
unsigned_aligned_apk_filename := app-release-unsigned-aligned.apk
signed_apk_filename := app-release.apk

build: $(output_dir)/$(unsigned_apk_filename)  ## Build an unsigned APK

$(output_dir)/$(unsigned_apk_filename):
	./gradlew assembleRelease

align: $(output_dir)/$(unsigned_aligned_apk_filename)  ## Align the unsigned APK

$(output_dir)/$(unsigned_aligned_apk_filename): $(output_dir)/$(unsigned_apk_filename)
	zipalign -v -p 4 "$<" "$@"

sign: $(output_dir)/$(signed_apk_filename)  ## Sign the aligned unsigned APK

$(output_dir)/$(signed_apk_filename): $(output_dir)/$(unsigned_aligned_apk_filename) | check-keystore-path
	apksigner sign --ks "$(keystore_path)" --out "$@" "$<"

.PHONY: install
install: $(output_dir)/$(signed_apk_filename)  ## Install the signed APK using adb
	adb -d install "$<"

.PHONY: check-keystore-path
check-keystore-path:
ifeq ($(keystore_path),)
	@echo "You must set the variable 'keystore_path'."
	exit 1
endif

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-16s\033[0m %s\n", $$1, $$2}'
