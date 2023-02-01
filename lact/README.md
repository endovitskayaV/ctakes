# lact

### Build

Execute from project root: `mvn clean package -DskipTests=true`

### Before running the code

- Add _umlsKey_ as environment variable. You can get it
  from [UMLS profile page](https://uts.nlm.nih.gov/uts/profile)(you need to
  be [registered](https://uts.nlm.nih.gov/uts/signup-login) - a license request needs to be filled - do it properly or
  it won\`t be accepted) or contact [Valentina Endovitskaya](mailto:evvendovitskaya@yandex.ru) to get her key

- [Download](https://disk.yandex.ru/d/4E0mBrImNdYOIg) resource and unarchive to the project root

### Notes

- .zip files were not updated from [svn repo](https://svn.apache.org/repos/asf/ctakes/trunk) (too heavy)`

### Dictionaries

- _sno_rx_16ab_:  SnowMed, RxNorm; default types _[default]_
- _base_lonic_:  SnowMed, RxNorm, LONIC; all types
- _all_types_:  SnowMed, RxNorm; all types
- _all_types_v1_: SnowMed, RxNorm; all types, fixed bug with incorrect labeling from unwanted dicts