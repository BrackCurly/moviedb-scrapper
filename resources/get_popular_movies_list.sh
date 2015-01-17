api_key=$(cat "api_key")

cd movies

for i in {1..1000}; do
curl -o $i.json http://api.themoviedb.org/3/movie/popular?api_key=$api_key&page=$i;
sleep 0.5s
done

cd ..
