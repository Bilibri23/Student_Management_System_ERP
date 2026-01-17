# Debugging API Errors - "Failed to Load" Issues

## Quick Checks

1. **Backend is Running?**
   - Check if Spring Boot server is running on `http://localhost:8080`
   - Test: Open `http://localhost:8080/api/auth/login` in browser (should return error about method, but not connection error)

2. **Frontend is Running?**
   - Check if Vite dev server is running on `http://localhost:3000`
   - Test: Open `http://localhost:3000` in browser

3. **Token is Valid?**
   - Open Browser DevTools → Application → Cookies
   - Check if `accessToken` cookie exists
   - If not, login again

4. **Check Browser Console**
   - Open DevTools → Console
   - Look for red error messages
   - Check Network tab → See failed requests

## Common Issues & Solutions

### Issue 1: CORS Error
**Symptom:** Console shows "CORS policy" error
**Solution:** Backend CORS config already allows `localhost:3000` and `localhost:5173`

### Issue 2: 401 Unauthorized
**Symptom:** All API calls return 401
**Solution:** 
- Token expired or invalid
- Logout and login again
- Check if token is being sent in Authorization header

### Issue 3: 500 Server Error
**Symptom:** Console shows server error
**Solution:** Check backend logs for detailed error

### Issue 4: Network Error / Connection Refused
**Symptom:** "Network Error" or "ECONNREFUSED"
**Solution:** Backend is not running - start it!

## Testing Steps

1. **Test Authentication:**
   ```javascript
   // In browser console after login:
   console.log(document.cookie)
   // Should see: accessToken=...
   ```

2. **Test API Directly:**
   ```javascript
   // In browser console:
   fetch('http://localhost:8080/api/dashboard/stats', {
     headers: {
       'Authorization': 'Bearer ' + document.cookie.split('accessToken=')[1]?.split(';')[0]
     }
   })
   .then(r => r.json())
   .then(console.log)
   ```

3. **Check Backend Logs:**
   - Look for any exceptions or errors when API is called
   - Check if username extraction is working

## Fixed Issues

1. ✅ Added better error logging in axios interceptor
2. ✅ Added detailed error display in Dashboard
3. ✅ Token is now correctly sent as `Bearer {token}`

## Next Steps

If still failing:
1. Check browser console for actual error message
2. Check Network tab → Failed requests → Response tab
3. Check backend logs for server-side errors
4. Verify user exists in database with correct username

