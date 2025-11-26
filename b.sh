clear
cd auth-react
npm run build
cd ..
rm -f auth/src/main/webapp/index.html
rm -rf auth/src/main/webapp/assets
cp -r auth-react/dist/* auth/src/main/webapp/
rm -rf auth-react/dist
cd auth
bash run.sh